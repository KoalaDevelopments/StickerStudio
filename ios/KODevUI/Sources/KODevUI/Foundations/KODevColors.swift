import SwiftUI

/// Raw brand colors. Prefer ``KODevColors`` in UI.
public enum KODevPalette {
    public static let background = Color(hex: 0x0E0721)
    public static let primary = Color(hex: 0x3B1FDD)
    public static let surface = Color(hex: 0x8851FA)
    public static let secondary = Color(hex: 0xD697FF)
    public static let neutral = Color(hex: 0xE8E8E8)
    public static let white = Color(hex: 0xFFFFFF)
}

/// Semantic color tokens of the KODev Design System. Each one switches between
/// the dark theme (the main one) and the light theme on its own, following the
/// system appearance or `.preferredColorScheme`.
public enum KODevColors {
    /// Page and full-screen background.
    public static let bg = Color(dark: 0x0E0721, light: 0xFFFFFF)
    /// Cards, panels and fields on `bg`.
    public static let bgRaised = Color(dark: 0x1A0E37, light: 0xE8E8E8)
    /// Main text and icons.
    public static let ink = Color(dark: 0xFFFFFF, light: 0x0E0721)
    /// Secondary text, captions and metadata.
    public static let inkMuted = Color(dark: 0xABA8B1, light: 0x625E6F)
    /// Titles and highlighted words: lilac on dark, primary on light.
    public static let inkAccent = Color(dark: 0xD697FF, light: 0x3B1FDD)
    /// Links in text. Alias of `inkAccent`.
    public static let link = inkAccent
    /// Fill of the main action.
    public static let accent = Color(hex: 0x3B1FDD)
    /// Hover of the main action.
    public static let accentHover = Color(hex: 0x8851FA)
    /// Main action while pressed.
    public static let accentPressed = Color(hex: 0x3019AE)
    /// Text and icons on `accent`, `accentHover` and `accentPressed`.
    public static let onAccent = Color(hex: 0xFFFFFF)
    /// Dividers and decorative outlines. Not for controls.
    public static let border = Color(dark: 0x352F45, light: 0xD8D7DB)
    /// Borders of fields, checkboxes and the secondary button.
    public static let borderStrong = Color(hex: 0x7A7785)
    /// Keyboard focus ring.
    public static let focusRing = Color(dark: 0xD697FF, light: 0x3B1FDD)
    /// Errors and destructive actions. Always with a word or an icon.
    public static let danger = Color(dark: 0xFF4245, light: 0xC4001A)
    /// Success and confirmation. Always with a word or an icon.
    public static let success = Color(dark: 0x4DD8E0, light: 0x00727D)
}

/// Brand gradients from the Design System.
public enum KODevGradients {
    public static let logo = LinearGradient(
        stops: [.init(color: KODevPalette.primary, location: 0.5), .init(color: KODevPalette.secondary, location: 1)],
        startPoint: .leading, endPoint: .trailing
    )
    public static let darken = LinearGradient(
        colors: [KODevPalette.primary, KODevPalette.surface], startPoint: .leading, endPoint: .trailing
    )
    /// Decorative only, never behind text.
    public static let foundation = LinearGradient(
        stops: [
            .init(color: KODevPalette.background, location: 0),
            .init(color: KODevPalette.primary, location: 0.25),
            .init(color: KODevPalette.surface, location: 0.5),
            .init(color: KODevPalette.secondary, location: 0.75),
        ],
        startPoint: .leading, endPoint: .trailing
    )
}

extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }

    init(dark: UInt32, light: UInt32) {
        #if canImport(UIKit)
        self.init(uiColor: UIColor { $0.userInterfaceStyle == .light ? UIColor(Color(hex: light)) : UIColor(Color(hex: dark)) })
        #else
        self.init(nsColor: NSColor(name: nil) { $0.bestMatch(from: [.aqua, .darkAqua]) == .aqua ? NSColor(Color(hex: light)) : NSColor(Color(hex: dark)) })
        #endif
    }
}
