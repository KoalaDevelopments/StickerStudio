import SwiftUI

/// A small square label in capitals for statuses and categories. Not tappable.
public struct KODevTag: View {
    public enum Tone: Sendable { case neutral, accent, success, danger }

    let text: String
    let tone: Tone

    public init(_ text: String, tone: Tone = .neutral) {
        self.text = text
        self.tone = tone
    }

    public var body: some View {
        let (frame, fill, ink): (Color, Color, Color) = switch tone {
        case .neutral: (KODevColors.borderStrong, .clear, KODevColors.ink)
        case .accent: (KODevColors.accent, KODevColors.accent, KODevColors.onAccent)
        case .success: (KODevColors.success, .clear, KODevColors.success)
        case .danger: (KODevColors.danger, .clear, KODevColors.danger)
        }
        Text(text.uppercased())
            .kodevType(KODevType.captionBold)
            .foregroundStyle(ink)
            .padding(.horizontal, KODevSpace.space2 - 1)
            .padding(.vertical, 1)
            .background(fill)
            .overlay { Rectangle().strokeBorder(frame, lineWidth: 1) }
    }
}
