import SwiftUI

/// The Design System's Button: square corners, IBM Plex Mono Bold, 48pt tall.
/// At most one `.primary` per screen.
///
/// ```swift
/// Button("Enviar") {}.buttonStyle(.kodev(.primary, block: true))
/// ```
public struct KODevButtonStyle: ButtonStyle {
    public enum Variant: Sendable { case primary, secondary, ghost }
    public enum Size: Sendable { case md, sm }

    let variant: Variant
    let size: Size
    let block: Bool

    public init(_ variant: Variant = .primary, size: Size = .md, block: Bool = false) {
        self.variant = variant
        self.size = size
        self.block = block
    }

    public func makeBody(configuration: Configuration) -> some View {
        StyledBody(configuration: configuration, style: self)
    }

    private struct StyledBody: View {
        let configuration: Configuration
        let style: KODevButtonStyle
        @Environment(\.isEnabled) private var isEnabled

        var body: some View {
            let (fill, ink, frame) = colors
            configuration.label
                .kodevType(style.size == .md ? KODevType.bold : KODevType.captionBold)
                .textCase(style.size == .sm ? .uppercase : nil)
                .lineLimit(1)
                .foregroundStyle(ink)
                .padding(.horizontal, style.size == .md ? KODevSpace.space6 : KODevSpace.space4)
                .frame(minWidth: 48, maxWidth: style.block ? .infinity : nil, minHeight: style.size == .md ? 48 : 34)
                .background(fill)
                .overlay { if let frame { Rectangle().strokeBorder(frame, lineWidth: 2) } }
                .contentShape(Rectangle())
        }

        private var colors: (Color, Color, Color?) {
            let pressed = configuration.isPressed
            guard isEnabled else { return (KODevColors.bgRaised, KODevColors.inkMuted, nil) }
            switch style.variant {
            case .primary: return (pressed ? KODevColors.accentPressed : KODevColors.accent, KODevColors.onAccent, nil)
            case .secondary: return (pressed ? KODevColors.bgRaised : .clear, KODevColors.ink, KODevColors.borderStrong)
            case .ghost: return (pressed ? KODevColors.bgRaised : .clear, KODevColors.inkAccent, nil)
            }
        }
    }
}

extension ButtonStyle where Self == KODevButtonStyle {
    /// The Design System's Button.
    public static func kodev(_ variant: KODevButtonStyle.Variant = .primary, size: KODevButtonStyle.Size = .md, block: Bool = false) -> KODevButtonStyle {
        KODevButtonStyle(variant, size: size, block: block)
    }
}
