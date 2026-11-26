import CoreGraphics
import Foundation
import ImageIO
@testable import StickerMedia
import Testing

private func tempURL() -> URL {
    FileManager.default.temporaryDirectory.appending(path: "\(UUID().uuidString).webp")
}

/// A width x height bitmap filled by `pixel(x, y)` returning straight RGBA.
private func makeImage(width: Int, height: Int, pixel: (Int, Int) -> (UInt8, UInt8, UInt8, UInt8)) -> CGImage {
    var bytes = [UInt8]()
    bytes.reserveCapacity(width * height * 4)
    for y in 0..<height {
        for x in 0..<width {
            let (r, g, b, a) = pixel(x, y)
            // Premultiply for CoreGraphics.
            bytes += [UInt8(Int(r) * Int(a) / 255), UInt8(Int(g) * Int(a) / 255), UInt8(Int(b) * Int(a) / 255), a]
        }
    }
    let provider = CGDataProvider(data: Data(bytes) as CFData)!
    return CGImage(
        width: width, height: height, bitsPerComponent: 8, bitsPerPixel: 32, bytesPerRow: width * 4,
        space: CGColorSpace(name: CGColorSpace.sRGB)!, bitmapInfo: CGBitmapInfo(rawValue: CGImageAlphaInfo.premultipliedLast.rawValue),
        provider: provider, decode: nil, shouldInterpolate: false, intent: .defaultIntent
    )!
}

/// Decodes the WebP at `url` and returns its size and the straight RGBA of pixel (x, y), top row first.
private func decodedPixel(_ url: URL, x: Int, y: Int) throws -> (width: Int, height: Int, rgba: [UInt8]) {
    let source = try #require(CGImageSourceCreateWithURL(url as CFURL, nil))
    #expect(CGImageSourceGetType(source) as String? == "org.webmproject.webp")
    let image = try #require(CGImageSourceCreateImageAtIndex(source, 0, nil))
    var pixel = [UInt8](repeating: 0, count: 4)
    pixel.withUnsafeMutableBytes { buffer in
        let context = CGContext(
            data: buffer.baseAddress, width: 1, height: 1, bitsPerComponent: 8, bytesPerRow: 4,
            space: CGColorSpace(name: CGColorSpace.sRGB)!, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
        )!
        // Shift the image so (x, y) lands on the single destination pixel.
        context.draw(image, in: CGRect(x: -x, y: y - image.height + 1, width: image.width, height: image.height))
    }
    return (image.width, image.height, pixel)
}

@Test func encodesArtworkOnTransparent512Canvas() async throws {
    // 300x150 opaque red: aspect-fit leaves transparent bands above and below.
    let url = tempURL()
    defer { try? FileManager.default.removeItem(at: url) }

    let file = try await StaticStickerEncoder.encode(makeImage(width: 300, height: 150) { _, _ in (255, 0, 0, 255) }, to: url)

    #expect(file.width == 512 && file.height == 512 && file.animation == nil)
    #expect(file.fileName == url.lastPathComponent)
    #expect(file.byteCount == (try Data(contentsOf: url)).count)
    #expect(file.byteCount <= 100 * 1024)
    let corner = try decodedPixel(url, x: 5, y: 5)
    #expect(corner.width == 512 && corner.height == 512)
    #expect(corner.rgba[3] == 0)
    let center = try decodedPixel(url, x: 256, y: 256).rgba
    #expect(center[3] == 255 && center[0] > 240 && center[1] < 15)
}

@Test func noiseIsSqueezedUnderTheStaticLimit() async throws {
    var generator = SystemRandomNumberGenerator()
    let noise = makeImage(width: 512, height: 512) { _, _ in
        (UInt8.random(in: 0...255, using: &generator), UInt8.random(in: 0...255, using: &generator), UInt8.random(in: 0...255, using: &generator), 255)
    }
    let url = tempURL()
    defer { try? FileManager.default.removeItem(at: url) }

    let file = try await StaticStickerEncoder.encode(noise, to: url)

    #expect(file.byteCount <= 100 * 1024)
}

@Test func cancelledEncodingThrows() async {
    let image = makeImage(width: 8, height: 8) { _, _ in (0, 0, 0, 255) }
    let task = Task {
        withUnsafeCurrentTask { $0?.cancel() }
        return try await StaticStickerEncoder.encode(image, to: tempURL())
    }
    await #expect(throws: CancellationError.self) { try await task.value }
}

@Test func searchPicksHighestQualityThatFits() throws {
    var tried: [Int] = []
    let data = try StaticStickerEncoder.fitWithin(100, maxQuality: 80) { quality in
        tried.append(quality)
        return Data(count: quality + 50)
    }
    #expect(data.count == 100)
    #expect(tried.first == 80 && tried.count <= 8)

    let first = try StaticStickerEncoder.fitWithin(100, maxQuality: 80) { Data(count: $0) }
    #expect(first.count == 80)

    #expect(throws: StickerEncodingError.tooLarge(bytes: 200, max: 100)) {
        try StaticStickerEncoder.fitWithin(100, maxQuality: 80) { Data(count: 200 + $0) }
    }
}
