import Foundation

/// Pack type is fixed at creation: the WhatsApp API forbids mixing static and animated stickers.
/// An enum (not a Bool) so a future kind (e.g. audio) is a new case, not a schema rewrite.
public enum PackKind: String, Codable, Sendable, CaseIterable {
    case `static`
    case animated
}

public struct Sticker: Identifiable, Codable, Sendable, Equatable {
    public var id: UUID
    /// Final exportable asset (512x512 WebP), relative to the app's sticker storage.
    public var fileName: String
    public var emojis: [String]
    public var accessibilityText: String?

    public init(id: UUID = UUID(), fileName: String, emojis: [String], accessibilityText: String? = nil) {
        self.id = id
        self.fileName = fileName
        self.emojis = emojis
        self.accessibilityText = accessibilityText
    }
}

public struct StickerPack: Identifiable, Codable, Sendable, Equatable {
    public var id: UUID
    /// WhatsApp `identifier`: stable, unique, < 128 chars of `a-z A-Z 0-9 _ - . space`.
    public var identifier: String
    public var name: String
    public var publisher: String
    public let kind: PackKind
    public var stickers: [Sticker]

    public init(id: UUID = UUID(), name: String, publisher: String, kind: PackKind, stickers: [Sticker] = []) {
        self.id = id
        self.identifier = id.uuidString
        self.name = name
        self.publisher = publisher
        self.kind = kind
        self.stickers = stickers
    }
}
