import CoreText
import SwiftUI

/// One text style of the type scale: size, line height, weight and tracking.
public struct KODevTextStyle: Sendable {
    public let size: CGFloat
    public let lineHeight: CGFloat
    public let bold: Bool
    public let tracking: CGFloat
    let relativeTo: Font.TextStyle

    /// The same style with other letter spacing, such as `caption` with 0 for
    /// sentences (field hints and errors).
    public func tracking(_ value: CGFloat) -> KODevTextStyle {
        KODevTextStyle(size: size, lineHeight: lineHeight, bold: bold, tracking: value, relativeTo: relativeTo)
    }

    /// The font, scaled with Dynamic Type.
    public var font: Font {
        KODevType.registerFonts()
        return .custom(bold ? "IBMPlexMono-Bold" : "IBMPlexMono-Regular", size: size, relativeTo: relativeTo)
    }
}

/// The type scale of the Design System, in IBM Plex Mono. No colors: text
/// inherits them. Apply with `.kodevType(.body)`.
public enum KODevType {
    public static let display = KODevTextStyle(size: 80, lineHeight: 60, bold: true, tracking: 0, relativeTo: .largeTitle)
    public static let headingLarge = KODevTextStyle(size: 40, lineHeight: 52, bold: true, tracking: 0, relativeTo: .largeTitle)
    public static let heading = KODevTextStyle(size: 32, lineHeight: 42, bold: true, tracking: 0, relativeTo: .title)
    public static let headingSmall = KODevTextStyle(size: 24, lineHeight: 31, bold: true, tracking: 0, relativeTo: .title2)
    public static let title = KODevTextStyle(size: 20, lineHeight: 26, bold: true, tracking: 0, relativeTo: .title3)
    public static let body = KODevTextStyle(size: 16, lineHeight: 22, bold: false, tracking: 0, relativeTo: .body)
    /// `body` in bold: buttons and words that stand out.
    public static let bold = KODevTextStyle(size: 16, lineHeight: 22, bold: true, tracking: 0, relativeTo: .body)
    /// Short labels and tags, preferably in capitals.
    public static let caption = KODevTextStyle(size: 12, lineHeight: 18, bold: false, tracking: 12 * 0.24, relativeTo: .caption)
    /// `caption` in bold, for tags and small buttons.
    public static let captionBold = KODevTextStyle(size: 12, lineHeight: 18, bold: true, tracking: 12 * 0.24, relativeTo: .caption)

    private static let registered: Void = {
        for name in ["IBMPlexMono-Regular", "IBMPlexMono-Bold"] {
            guard let url = Bundle.module.url(forResource: name, withExtension: "ttf") else { continue }
            CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
        }
    }()

    /// Registers the bundled fonts. Safe to call many times.
    public static func registerFonts() { registered }
}

extension View {
    /// Applies a style of the type scale.
    public func kodevType(_ style: KODevTextStyle) -> some View {
        // ponytail: line spacing approximated as lineHeight minus 1.3x size
        // (Plex Mono's natural line). Exact line boxes need UIKit text.
        font(style.font)
            .tracking(style.tracking)
            .lineSpacing(max(0, style.lineHeight - style.size * 1.3))
    }
}
