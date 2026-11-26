import SwiftUI

/// One option of a group where only one counts: a 28pt pixel circle and its
/// label. The whole row is tappable and at least 48pt tall. Put the group
/// under a label naming the question ("Tipo do pack").
public struct KODevRadio: View {
    let label: String
    let isSelected: Bool
    let action: () -> Void
    @Environment(\.isEnabled) private var isEnabled

    public init(_ label: String, isSelected: Bool, action: @escaping () -> Void) {
        self.label = label
        self.isSelected = isSelected
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            HStack(spacing: KODevSpace.space3) {
                ZStack {
                    KODevPixelArt(rows: Self.ring)
                    if isSelected { KODevPixelArt(rows: Self.dot) }
                }
                .frame(width: 28, height: 28)
                .foregroundStyle(!isEnabled ? KODevColors.border : isSelected ? KODevColors.accent : KODevColors.borderStrong)
                Text(label)
                    .kodevType(KODevType.body)
                    .foregroundStyle(isEnabled ? KODevColors.ink : KODevColors.inkMuted)
                Spacer(minLength: 0)
            }
            .frame(minHeight: 48)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? [.isSelected] : [])
    }

    static let ring = [".....####.....", "...##....##...", "..#........#..", ".#..........#.", ".#..........#.", "#............#", "#............#",
                       "#............#", "#............#", ".#..........#.", ".#..........#.", "..#........#..", "...##....##...", ".....####....."]
    static let dot = ["..............", "..............", "..............", "..............", ".....####.....", "....######....", "....######....",
                      "....######....", "....######....", ".....####.....", "..............", "..............", "..............", ".............."]
}
