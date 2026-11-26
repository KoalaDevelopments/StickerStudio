import StickerDomain
import SwiftUI

@main
struct StickerStudioApp: App {
    var body: some Scene {
        WindowGroup {
            NavigationStack {
                ContentUnavailableView("Nenhum pack ainda", systemImage: "face.smiling")
                    .navigationTitle("StickerStudio")
            }
        }
    }
}
