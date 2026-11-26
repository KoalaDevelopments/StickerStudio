import SwiftUI

/// The top bar of an app screen, 56pt: an optional control on the left, the
/// title in `title` style (never centered) and up to three actions.
public struct KODevAppBar<Leading: View, Actions: View>: View {
    let title: String
    let leading: Leading
    let actions: Actions

    public init(_ title: String, @ViewBuilder leading: () -> Leading = { EmptyView() }, @ViewBuilder actions: () -> Actions = { EmptyView() }) {
        self.title = title
        self.leading = leading()
        self.actions = actions()
    }

    public var body: some View {
        HStack(spacing: 0) {
            leading
            Text(title)
                .kodevType(KODevType.title)
                .foregroundStyle(KODevColors.ink)
                .lineLimit(1)
                .truncationMode(.tail)
                .padding(.leading, Leading.self == EmptyView.self ? KODevSpace.space4 : 0)
                .accessibilityAddTraits(.isHeader)
            Spacer(minLength: KODevSpace.space2)
            actions
        }
        .frame(height: 56)
        .background(KODevColors.bg)
        .overlay(alignment: .bottom) { KODevColors.border.frame(height: 1) }
    }
}

extension View {
    /// Pins a ``KODevAppBar`` above the screen in place of the navigation bar.
    /// The swipe-back gesture keeps working.
    public func kodevAppBar<Leading: View, Actions: View>(_ bar: KODevAppBar<Leading, Actions>) -> some View {
        #if os(iOS)
        toolbar(.hidden, for: .navigationBar)
            .safeAreaInset(edge: .top, spacing: 0) { bar }
        #else
        safeAreaInset(edge: .top, spacing: 0) { bar }
        #endif
    }
}

#if canImport(UIKit)
// Hiding the navigation bar turns off UIKit's swipe-back. This brings it back.
extension UINavigationController: @retroactive UIGestureRecognizerDelegate {
    override open func viewDidLoad() {
        super.viewDidLoad()
        interactivePopGestureRecognizer?.delegate = self
    }

    public func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        viewControllers.count > 1
    }
}
#endif
