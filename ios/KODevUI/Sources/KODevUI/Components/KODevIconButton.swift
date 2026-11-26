import SwiftUI

/// A square button with an interface icon and no text, 48 by 48pt.
public struct KODevIconButton: View {
    let icon: [String]
    let label: String
    let variant: KODevButtonStyle.Variant
    let action: () -> Void

    /// - Parameter label: The action's name ("Voltar"). Required: it is what
    ///   VoiceOver says, since the button has no visible text.
    public init(_ icon: [String], label: String, variant: KODevButtonStyle.Variant = .ghost, action: @escaping () -> Void) {
        self.icon = icon
        self.label = label
        self.variant = variant
        self.action = action
    }

    public var body: some View {
        Button(action: action) { KODevIcon(icon) }
            .buttonStyle(IconStyle(variant: variant))
            .accessibilityLabel(label)
    }

    private struct IconStyle: ButtonStyle {
        let variant: KODevButtonStyle.Variant
        @Environment(\.isEnabled) private var isEnabled

        func makeBody(configuration: Configuration) -> some View {
            let pressed = configuration.isPressed
            let fill: Color = !isEnabled ? KODevColors.bgRaised
                : variant == .primary ? (pressed ? KODevColors.accentPressed : KODevColors.accent)
                : pressed ? KODevColors.bgRaised : .clear
            let ink: Color = !isEnabled ? KODevColors.inkMuted : variant == .primary ? KODevColors.onAccent : KODevColors.ink
            configuration.label
                .foregroundStyle(ink)
                .frame(width: 48, height: 48)
                .background(fill)
                .overlay {
                    if variant == .secondary && isEnabled { Rectangle().strokeBorder(KODevColors.borderStrong, lineWidth: 2) }
                }
                .contentShape(Rectangle())
        }
    }
}
