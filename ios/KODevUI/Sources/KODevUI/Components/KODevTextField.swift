import SwiftUI

/// A text field with a label on top, an optional hint and an error message, in
/// the same 2pt square frame as the secondary button.
public struct KODevTextField: View {
    let label: String
    @Binding var text: String
    let hint: String?
    let error: String?
    let placeholder: String

    @FocusState private var focused: Bool
    @Environment(\.isEnabled) private var isEnabled

    /// - Parameters:
    ///   - label: The data's name ("Nome do pack"), not an instruction.
    ///   - error: How to fix the value. Replaces the hint and turns the frame `danger`.
    public init(_ label: String, text: Binding<String>, hint: String? = nil, error: String? = nil, placeholder: String = "") {
        self.label = label
        self._text = text
        self.hint = hint
        self.error = error
        self.placeholder = placeholder
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: KODevSpace.space2) {
            Text(label.uppercased())
                .kodevType(KODevType.captionBold)
                .foregroundStyle(KODevColors.ink)
            TextField(label, text: $text, prompt: Text(placeholder).foregroundStyle(KODevColors.inkMuted))
                .focused($focused)
                .kodevType(KODevType.body)
                .foregroundStyle(isEnabled ? KODevColors.ink : KODevColors.inkMuted)
                .padding(.horizontal, KODevSpace.space4)
                .frame(minHeight: 48)
                .background(isEnabled ? Color.clear : KODevColors.bgRaised)
                .overlay { if isEnabled { Rectangle().strokeBorder(frame, lineWidth: 2) } }
                .accessibilityHint(error ?? hint ?? "")
            if let message = error ?? hint {
                Text(message)
                    .kodevType(KODevType.caption.tracking(0))
                    .foregroundStyle(error == nil ? KODevColors.inkMuted : KODevColors.danger)
            }
        }
    }

    private var frame: Color {
        error != nil ? KODevColors.danger : focused ? KODevColors.focusRing : KODevColors.borderStrong
    }
}
