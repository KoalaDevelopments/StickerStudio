import SwiftUI

/// A panel that groups content: `bgRaised`, a `border` line, `radius-md`
/// corners and `space-6` padding. No shadow. Wrap it in a `Button` or
/// `NavigationLink` to make the whole card tappable.
public struct KODevCard<Content: View>: View {
    let content: Content

    public init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: KODevRadius.md)
        content
            .foregroundStyle(KODevColors.ink)
            .padding(KODevSpace.space6)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(KODevColors.bgRaised, in: shape)
            .overlay { shape.strokeBorder(KODevColors.border, lineWidth: 1) }
    }
}
