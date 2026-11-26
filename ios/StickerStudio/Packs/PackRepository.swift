import Foundation
import StickerDomain

/// Where the screens read and write packs. The in-memory one stands in until
/// the core's persistence (#3) lands; then an adapter over it replaces it.
protocol PackRepository: Sendable {
    func packs() async -> [StickerPack]
    func save(_ pack: StickerPack) async
    func delete(id: StickerPack.ID) async
}

actor InMemoryPackRepository: PackRepository {
    private var storage: [StickerPack]

    init(_ packs: [StickerPack] = []) {
        storage = packs
    }

    func packs() -> [StickerPack] { storage }

    func save(_ pack: StickerPack) {
        if let index = storage.firstIndex(where: { $0.id == pack.id }) {
            storage[index] = pack
        } else {
            storage.append(pack)
        }
    }

    func delete(id: StickerPack.ID) {
        storage.removeAll { $0.id == id }
    }
}

extension InMemoryPackRepository {
    /// Sample packs covering every list state, for development and previews.
    static var sample: InMemoryPackRepository {
        func stickers(_ count: Int, kind: PackKind) -> [Sticker] {
            (0..<count).map { i in
                Sticker(
                    image: ImageFile(
                        fileName: "\(i).webp", byteCount: 40_000, width: 512, height: 512,
                        animation: kind == .animated ? .init(durationMs: 2_000, shortestFrameMs: 40) : nil
                    ),
                    emojis: ["😺"]
                )
            }
        }
        let tray = ImageFile(fileName: "tray.png", byteCount: 8_000, width: 96, height: 96)
        return InMemoryPackRepository([
            StickerPack(name: "Meus gatos", publisher: "Koala", kind: .static, tray: tray, stickers: stickers(12, kind: .static)),
            StickerPack(name: "Reações do trabalho", publisher: "Koala", kind: .animated, tray: tray, stickers: stickers(5, kind: .animated)),
            StickerPack(name: "Viagem 2026", publisher: "Koala", kind: .static, stickers: stickers(2, kind: .static)),
            StickerPack(name: "Memes da família", publisher: "Koala", kind: .static, stickers: stickers(4, kind: .static)),
        ])
    }
}
