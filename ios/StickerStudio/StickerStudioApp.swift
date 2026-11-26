import KODevUI
import SwiftUI

@main
struct StickerStudioApp: App {
    // ponytail: in-memory sample packs until the core persistence (#3) lands.
    private let repository: PackRepository = InMemoryPackRepository.sample
    @State private var path: [Route] = []

    enum Route: Hashable { case gallery }

    var body: some Scene {
        WindowGroup {
            NavigationStack(path: $path) {
                PackListView(repository: repository, onOpenGallery: galleryAction)
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case .gallery: DesignSystemGallery()
                        }
                    }
            }
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
