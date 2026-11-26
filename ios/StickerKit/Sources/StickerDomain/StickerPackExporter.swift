/// What the current export path can carry. Audio stays false until WhatsApp publishes the API
/// (github.com/WhatsApp/stickers/issues/1163); then the implementation and flag change, not the UI.
public struct ExportCapabilities: Sendable, Equatable {
    public var supportsAudio: Bool

    public init(supportsAudio: Bool) {
        self.supportsAudio = supportsAudio
    }
}

public protocol StickerPackExporter: Sendable {
    var capabilities: ExportCapabilities { get }
    func export(_ pack: StickerPack) async throws
}
