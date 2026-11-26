import SwiftUI

extension View {
    /// Puts a screen in the Design System: fonts registered, `bg` behind it,
    /// `ink` as the default foreground, `accent` as the tint and body text.
    public func kodevTheme() -> some View {
        KODevType.registerFonts()
        return self
            .kodevType(KODevType.body)
            .foregroundStyle(KODevColors.ink)
            .tint(KODevColors.accent)
            .background(KODevColors.bg.ignoresSafeArea())
    }
}
