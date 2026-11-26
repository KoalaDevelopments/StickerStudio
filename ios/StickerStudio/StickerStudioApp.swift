import KODevUI
import StickerDomain
import SwiftUI

@main
struct StickerStudioApp: App {
    // ponytail: in-memory sample packs until the core persistence (#3) lands.
    private let repository: PackRepository = InMemoryPackRepository.sample
    @State private var path: [Route] = []
    @State private var toast: String?

    enum Route: Hashable {
        case gallery, createPack
        case pack(UUID), editPack(UUID)
    }

    var body: some Scene {
        WindowGroup {
            NavigationStack(path: $path) {
                PackListView(repository: repository, onCreate: { path.append(.createPack) }, onOpen: { path.append(.pack($0.id)) }, onOpenGallery: galleryAction)
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case .gallery: DesignSystemGallery()
                        case .createPack: PackFormView(repository: repository) { path.removeLast() }
                        case .pack(let id):
                            PackDetailView(
                                repository: repository, packID: id,
                                onEdit: { path.append(.editPack($0.id)) },
                                onAddSticker: { _ in toast = "O editor de figurinhas chega na próxima etapa." },
                                onAddToWhatsApp: { _ in toast = "O envio ao WhatsApp chega com a integração do core." },
                                onClose: { path.removeLast() }
                            )
                        case .editPack(let id): EditPackLoader(repository: repository, id: id) { path.removeLast() }
                        }
                    }
            }
            .kodevToast($toast)
            .kodevTheme()
        }
    }

    private var galleryAction: (() -> Void)? {
        #if DEBUG
        { path.append(.gallery) }
        #else
        nil
        #endif
    }
}

/// Loads a pack by id for the form, which needs the pack up front.
private struct EditPackLoader: View {
    let repository: PackRepository
    let id: UUID
    let onDone: () -> Void
    @State private var pack: StickerPack?

    var body: some View {
        Group {
            if let pack { PackFormView(repository: repository, pack: pack, onDone: onDone) } else { KODevColors.bg }
        }
        .task { pack = await repository.packs().first { $0.id == id } }
    }
}
