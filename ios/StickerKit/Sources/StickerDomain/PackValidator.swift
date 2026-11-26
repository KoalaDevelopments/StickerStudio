import Foundation

/// WhatsApp third-party sticker limits (docs/escopo-mvp.md 4.1).
/// Same values as `spec/whatsapp-limits.json`; the tests fail if they drift apart.
/// Text lengths count UTF-16 code units, like `String.length` on Android.
public enum WhatsAppLimits {
    public static let stickersPerPack = 3...30
    /// Name and publisher.
    public static let textLength = 1...128
    public static let maxIdentifierLength = 127
    /// The whole identifier must match.
    public static let identifierPattern = "[A-Za-z0-9_.\\- ]+"
    public static let trayDimension = 96
    public static let maxTrayBytes = 50 * 1024
    public static let stickerDimension = 512
    public static let minFrameDurationMs = 8
    public static let maxAnimationDurationMs = 10_000
    public static let emojisPerSticker = 1...3

    public static func maxStickerBytes(_ kind: PackKind) -> Int {
        switch kind {
        case .static: 100 * 1024
        case .animated: 500 * 1024
        }
    }

    public static func maxAccessibilityTextLength(_ kind: PackKind) -> Int {
        switch kind {
        case .static: 125
        case .animated: 255
        }
    }
}

/// One broken WhatsApp rule. Each case carries the actual value and the limit, so the UI can say
/// exactly what to fix. `index` is the sticker's position in `StickerPack.stickers`, from 0.
public enum PackIssue: Equatable, Sendable {
    case stickerCount(Int, allowed: ClosedRange<Int>)
    case nameLength(Int, allowed: ClosedRange<Int>)
    case publisherLength(Int, allowed: ClosedRange<Int>)
    case invalidIdentifier(String)
    case missingTray
    case trayDimensions(width: Int, height: Int, required: Int)
    case trayBytes(Int, max: Int)
    case stickerDimensions(index: Int, width: Int, height: Int, required: Int)
    case stickerBytes(index: Int, Int, max: Int)
    /// A static image in an animated pack, or the other way around.
    case stickerKind(index: Int, expected: PackKind)
    case frameDuration(index: Int, ms: Int, min: Int)
    case animationDuration(index: Int, ms: Int, max: Int)
    case emojiCount(index: Int, Int, allowed: ClosedRange<Int>)
    case accessibilityTextLength(index: Int, Int, max: Int)
}

extension StickerPack {
    /// Every WhatsApp rule this pack breaks, pack-level first, then tray, then stickers in order.
    /// Empty means the pack can be exported.
    public func validate() -> [PackIssue] {
        typealias L = WhatsAppLimits
        var issues: [PackIssue] = []

        if !L.stickersPerPack.contains(stickers.count) {
            issues.append(.stickerCount(stickers.count, allowed: L.stickersPerPack))
        }
        if !L.textLength.contains(name.utf16.count) {
            issues.append(.nameLength(name.utf16.count, allowed: L.textLength))
        }
        if !L.textLength.contains(publisher.utf16.count) {
            issues.append(.publisherLength(publisher.utf16.count, allowed: L.textLength))
        }
        let match = identifier.range(of: L.identifierPattern, options: .regularExpression)
        if identifier.utf16.count > L.maxIdentifierLength || match != identifier.startIndex..<identifier.endIndex {
            issues.append(.invalidIdentifier(identifier))
        }

        if let tray {
            if tray.width != L.trayDimension || tray.height != L.trayDimension {
                issues.append(.trayDimensions(width: tray.width, height: tray.height, required: L.trayDimension))
            }
            if tray.byteCount > L.maxTrayBytes {
                issues.append(.trayBytes(tray.byteCount, max: L.maxTrayBytes))
            }
        } else {
            issues.append(.missingTray)
        }

        for (index, sticker) in stickers.enumerated() {
            let image = sticker.image
            if image.width != L.stickerDimension || image.height != L.stickerDimension {
                issues.append(.stickerDimensions(index: index, width: image.width, height: image.height, required: L.stickerDimension))
            }
            if image.byteCount > L.maxStickerBytes(kind) {
                issues.append(.stickerBytes(index: index, image.byteCount, max: L.maxStickerBytes(kind)))
            }
            switch (kind, image.animation) {
            case (.static, nil):
                break
            case (.animated, let animation?):
                if animation.shortestFrameMs < L.minFrameDurationMs {
                    issues.append(.frameDuration(index: index, ms: animation.shortestFrameMs, min: L.minFrameDurationMs))
                }
                if animation.durationMs > L.maxAnimationDurationMs {
                    issues.append(.animationDuration(index: index, ms: animation.durationMs, max: L.maxAnimationDurationMs))
                }
            default:
                issues.append(.stickerKind(index: index, expected: kind))
            }
            if !L.emojisPerSticker.contains(sticker.emojis.count) {
                issues.append(.emojiCount(index: index, sticker.emojis.count, allowed: L.emojisPerSticker))
            }
            if let text = sticker.accessibilityText, text.utf16.count > L.maxAccessibilityTextLength(kind) {
                issues.append(.accessibilityTextLength(index: index, text.utf16.count, max: L.maxAccessibilityTextLength(kind)))
            }
        }
        return issues
    }
}
