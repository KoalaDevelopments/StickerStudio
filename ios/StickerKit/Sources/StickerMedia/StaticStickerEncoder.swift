import CoreGraphics
import Foundation
import libwebp
import StickerDomain

public enum StickerEncodingError: Error, Equatable {
    /// Even the lowest quality is over the limit.
    case tooLarge(bytes: Int, max: Int)
    case encodingFailed
}

public enum StaticStickerEncoder {
    /// Highest lossy quality tried. Smaller files send faster (WhatsApp's own stickers are about 15 KB).
    // ponytail: fixed cap, lossy only. Try lossless for flat artwork if text or outlines look soft.
    static let maxQuality = 80

    /// Draws `image` centered and aspect-fit on a transparent 512x512 canvas and writes it to `url` as WebP,
    /// at the highest quality up to `maxQuality` that fits the static sticker limit. Checks for cancellation
    /// between encoding attempts.
    public static func encode(_ image: CGImage, to url: URL) async throws -> ImageFile {
        try Task.checkCancellation()
        let side = WhatsAppLimits.stickerDimension
        let rgba = try straightRGBA(image, side: side)
        let data = try fitWithin(WhatsAppLimits.maxStickerBytes(.static), maxQuality: maxQuality) { quality in
            try Task.checkCancellation()
            return try webp(rgba, side: side, quality: quality)
        }
        try data.write(to: url, options: .atomic)
        return ImageFile(fileName: url.lastPathComponent, byteCount: data.count, width: side, height: side)
    }

    /// Tries `maxQuality` first, then binary-searches lower qualities for the highest one under `maxBytes`.
    static func fitWithin(_ maxBytes: Int, maxQuality: Int, encode: (Int) throws -> Data) throws -> Data {
        let first = try encode(maxQuality)
        if first.count <= maxBytes { return first }
        var low = 0
        var high = maxQuality - 1
        var best: Data?
        var lastSize = first.count
        while low <= high {
            let quality = (low + high) / 2
            let data = try encode(quality)
            if data.count <= maxBytes {
                best = data
                low = quality + 1
            } else {
                lastSize = data.count
                high = quality - 1
            }
        }
        guard let best else { throw StickerEncodingError.tooLarge(bytes: lastSize, max: maxBytes) }
        return best
    }

    private static func webp(_ rgba: [UInt8], side: Int, quality: Int) throws -> Data {
        var output: UnsafeMutablePointer<UInt8>?
        let size = rgba.withUnsafeBufferPointer {
            WebPEncodeRGBA($0.baseAddress, Int32(side), Int32(side), Int32(side * 4), Float(quality), &output)
        }
        guard size > 0, let output else { throw StickerEncodingError.encodingFailed }
        defer { WebPFree(output) }
        return Data(bytes: output, count: size)
    }

    /// RGBA pixels, top row first, with straight (not premultiplied) alpha as WebP expects.
    private static func straightRGBA(_ image: CGImage, side: Int) throws -> [UInt8] {
        var pixels = [UInt8](repeating: 0, count: side * side * 4)
        let drawn = pixels.withUnsafeMutableBytes { buffer -> Bool in
            guard let context = CGContext(
                data: buffer.baseAddress, width: side, height: side, bitsPerComponent: 8, bytesPerRow: side * 4,
                space: CGColorSpace(name: CGColorSpace.sRGB)!, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
            ) else { return false }
            context.interpolationQuality = .high
            let scale = min(Double(side) / Double(image.width), Double(side) / Double(image.height))
            let width = Double(image.width) * scale
            let height = Double(image.height) * scale
            context.draw(image, in: CGRect(x: (Double(side) - width) / 2, y: (Double(side) - height) / 2, width: width, height: height))
            return true
        }
        guard drawn else { throw StickerEncodingError.encodingFailed }
        // CoreGraphics only draws premultiplied alpha. Fully transparent pixels are already 0.
        for i in stride(from: 0, to: pixels.count, by: 4) {
            let alpha = Int(pixels[i + 3])
            guard alpha > 0, alpha < 255 else { continue }
            for c in i..<(i + 3) {
                pixels[c] = UInt8(min(255, (Int(pixels[c]) * 255 + alpha / 2) / alpha))
            }
        }
        return pixels
    }
}
