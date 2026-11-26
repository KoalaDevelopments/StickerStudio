import KODevUI
import StickerDomain
import SwiftUI

@main
struct StickerStudioApp: App {
    var body: some Scene {
        WindowGroup {
            // Temporary root while the MVP screens are built: the Design
            // System gallery, to review the KODevUI layer on a device.
            NavigationStack {
                DesignSystemGallery()
            }
            .kodevTheme()
        }
    }
}
