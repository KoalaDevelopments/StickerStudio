import Foundation

/// Pack type is fixed at creation: the WhatsApp API forbids mixing static and animated stickers.
/// An enum (not a Bool) so a future kind (e.g. audio) is a new case, not a schema rewrite.
public enum PackKind: String, Codable, Sendable, CaseIterable {
    case `static`
    case animated
}

/// An encoded file in the app's sticker storage, with the facts the encoder measured when writing it.
public struct ImageFile: Codable, Sendable, Equatable {
    public var fileName: String
    public var byteCount: Int
    public var width: Int
    public var height: Int
    /// Present only for animated images.
    public var animation: Animation?

    public struct Animation: Codable, Sendable, Equatable {
        public var durationMs: Int
        public var shortestFrameMs: Int

        public init(durationMs: Int, shortestFrameMs: Int) {
            self.durationMs = durationMs
            self.shortestFrameMs = shortestFrameMs
        }
    }

    public init(fileName: String, byteCount: Int, width: Int, height: Int, animation: Animation? = nil) {
        self.fileName = fileName
        self.byteCount = byteCount
        self.width = width
        self.height = height
        self.animation = animation
    }
}

public struct Sticker: Identifiable, Codable, Sendable, Equatable {
    public var id: UUID
    /// Final exportable asset (512x512 WebP).
    public var image: ImageFile
    public var emojis: [String]
    public var accessibilityText: String?

    public init(id: UUID = UUID(), image: ImageFile, emojis: [String], accessibilityText: String? = nil) {
        self.id = id
        self.image = image
        self.emojis = emojis
        self.accessibilityText = accessibilityText
    }
}

public struct StickerPack: Identifiable, Codable, Sendable, Equatable {
    public var id: UUID
    /// WhatsApp `identifier`: stable and unique. See `WhatsAppLimits.identifierPattern`.
    public var identifier: String
    public var name: String
    public var publisher: String
    public let kind: PackKind
    /// 96x96 PNG shown in the WhatsApp sticker tray. Nil until generated.
    public var tray: ImageFile?
    public var stickers: [Sticker]

    public init(
        id: UUID = UUID(),
        name: String,
        publisher: String,
        kind: PackKind,
        tray: ImageFile? = nil,
        stickers: [Sticker] = []
    ) {
        self.id = id
        self.identifier = id.uuidString
        self.name = name
        self.publisher = publisher
        self.kind = kind
        self.tray = tray
        self.stickers = stickers
    }
}
