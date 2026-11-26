import Foundation
import StickerDomain
import Testing

private let tray = ImageFile(fileName: "tray.png", byteCount: 51_200, width: 96, height: 96)
private let motion = ImageFile.Animation(durationMs: 3_000, shortestFrameMs: 40)

private func image(bytes: Int = 20_000, side: Int = 512, animation: ImageFile.Animation? = nil) -> ImageFile {
    ImageFile(fileName: "sticker.webp", byteCount: bytes, width: side, height: side, animation: animation)
}

@Test func limitsMatchSharedSpec() throws {
    let url = URL(filePath: "../../../../spec/whatsapp-limits.json", relativeTo: URL(filePath: #filePath))
    let spec = try JSONDecoder().decode(Spec.self, from: Data(contentsOf: url))
    typealias L = WhatsAppLimits

    #expect(spec.stickersPerPack.range == L.stickersPerPack)
    #expect(spec.pack.textLength.range == L.textLength)
    #expect(spec.pack.maxIdentifierLength == L.maxIdentifierLength)
    #expect(spec.pack.identifierPattern == L.identifierPattern)
    #expect(spec.tray.dimension == L.trayDimension)
    #expect(spec.tray.maxBytes == L.maxTrayBytes)
    #expect(spec.sticker.dimension == L.stickerDimension)
    #expect(spec.sticker.maxBytes.static == L.maxStickerBytes(.static))
    #expect(spec.sticker.maxBytes.animated == L.maxStickerBytes(.animated))
    #expect(spec.sticker.minFrameDurationMs == L.minFrameDurationMs)
    #expect(spec.sticker.maxAnimationDurationMs == L.maxAnimationDurationMs)
    #expect(spec.sticker.emojis.range == L.emojisPerSticker)
    #expect(spec.sticker.maxAccessibilityTextLength.static == L.maxAccessibilityTextLength(.static))
    #expect(spec.sticker.maxAccessibilityTextLength.animated == L.maxAccessibilityTextLength(.animated))
}

@Test func packsAtTheLimitsHaveNoIssues() {
    // 64 emoji = 128 UTF-16 code units, the maximum name length.
    var still = StickerPack(
        name: String(repeating: "😀", count: 64), publisher: "P", kind: .static, tray: tray,
        stickers: (0..<3).map { _ in
            Sticker(image: image(bytes: 102_400), emojis: ["😀", "😂", "🎉"], accessibilityText: String(repeating: "a", count: 125))
        }
    )
    still.identifier = String(repeating: "a", count: 126) + "-"
    #expect(still.validate() == [])

    let moving = StickerPack(
        name: "Pack", publisher: "Me", kind: .animated, tray: tray,
        stickers: (0..<30).map { _ in
            Sticker(
                image: image(bytes: 512_000, animation: .init(durationMs: 10_000, shortestFrameMs: 8)),
                emojis: ["🎉"], accessibilityText: String(repeating: "a", count: 255)
            )
        }
    )
    #expect(moving.validate() == [])
}

@Test func brokenStaticPackReportsEveryRule() {
    var pack = StickerPack(
        name: String(repeating: "😀", count: 65), publisher: "", kind: .static,
        tray: ImageFile(fileName: "tray.png", byteCount: 51_201, width: 100, height: 96),
        stickers: [
            Sticker(image: image(bytes: 102_401, side: 511), emojis: []),
            Sticker(image: image(animation: motion), emojis: ["😀"], accessibilityText: String(repeating: "a", count: 126)),
        ]
    )
    pack.identifier = "bad/id"

    #expect(pack.validate() == [
        .stickerCount(2, allowed: 3...30),
        .nameLength(130, allowed: 1...128),
        .publisherLength(0, allowed: 1...128),
        .invalidIdentifier("bad/id"),
        .trayDimensions(width: 100, height: 96, required: 96),
        .trayBytes(51_201, max: 51_200),
        .stickerDimensions(index: 0, width: 511, height: 511, required: 512),
        .stickerBytes(index: 0, 102_401, max: 102_400),
        .emojiCount(index: 0, 0, allowed: 1...3),
        .stickerKind(index: 1, expected: .static),
        .accessibilityTextLength(index: 1, 126, max: 125),
    ])
}

@Test func brokenAnimatedPackReportsEveryRule() {
    var pack = StickerPack(
        name: "Pack", publisher: "Me", kind: .animated,
        stickers: [
            Sticker(
                image: image(bytes: 512_001, animation: .init(durationMs: 10_001, shortestFrameMs: 7)),
                emojis: ["1", "2", "3", "4"], accessibilityText: String(repeating: "a", count: 256)
            ),
            Sticker(image: image(), emojis: ["😀"]),
            Sticker(image: image(animation: motion), emojis: ["😀"]),
        ]
    )
    pack.identifier = String(repeating: "a", count: 128)

    #expect(pack.validate() == [
        .invalidIdentifier(String(repeating: "a", count: 128)),
        .missingTray,
        .stickerBytes(index: 0, 512_001, max: 512_000),
        .frameDuration(index: 0, ms: 7, min: 8),
        .animationDuration(index: 0, ms: 10_001, max: 10_000),
        .emojiCount(index: 0, 4, allowed: 1...3),
        .accessibilityTextLength(index: 0, 256, max: 255),
        .stickerKind(index: 1, expected: .animated),
    ])
}

/// Shape of spec/whatsapp-limits.json.
private struct Spec: Decodable {
    struct Bounds: Decodable {
        let min: Int
        let max: Int
        var range: ClosedRange<Int> { min...max }
    }
    struct PerKind: Decodable {
        let `static`: Int
        let animated: Int
    }
    struct Pack: Decodable {
        let textLength: Bounds
        let maxIdentifierLength: Int
        let identifierPattern: String
    }
    struct Tray: Decodable {
        let dimension: Int
        let maxBytes: Int
    }
    struct Sticker: Decodable {
        let dimension: Int
        let maxBytes: PerKind
        let minFrameDurationMs: Int
        let maxAnimationDurationMs: Int
        let emojis: Bounds
        let maxAccessibilityTextLength: PerKind
    }
    let stickersPerPack: Bounds
    let pack: Pack
    let tray: Tray
    let sticker: Sticker
}
