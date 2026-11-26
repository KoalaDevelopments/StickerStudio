import SwiftUI

/// A short feedback message after an action: a `bgRaised` strip with a
/// `borderStrong` line and a 4pt bar on the left in the tone's color.
public struct KODevToast: View {
    public enum Tone: Sendable { case neutral, success, danger }

    let text: String
    let tone: Tone

    public init(_ text: String, tone: Tone = .neutral) {
        self.text = text
        self.tone = tone
    }

    public var body: some View {
        let bar: Color = switch tone {
        case .neutral: KODevColors.accent
        case .success: KODevColors.success
        case .danger: KODevColors.danger
        }
        HStack(spacing: 0) {
            bar.frame(width: 4)
            Text(text)
                .kodevType(KODevType.body)
                .foregroundStyle(KODevColors.ink)
                .padding(KODevSpace.space4)
            Spacer(minLength: 0)
        }
        .frame(minHeight: 56)
        .fixedSize(horizontal: false, vertical: true)
        .background(KODevColors.bgRaised)
        .overlay { Rectangle().strokeBorder(KODevColors.borderStrong, lineWidth: 1) }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.updatesFrequently)
    }
}

extension View {
    /// Shows ``KODevToast`` `message` at the bottom for 4 seconds, one at a time.
    /// Set `message` to nil to hide it early.
    public func kodevToast(_ message: Binding<String?>, tone: KODevToast.Tone = .neutral) -> some View {
        overlay(alignment: .bottom) {
            if let text = message.wrappedValue {
                KODevToast(text, tone: tone)
                    .padding(KODevSpace.space4)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .task(id: text) {
                        try? await Task.sleep(for: .seconds(4))
                        message.wrappedValue = nil
                    }
            }
        }
        .animation(.easeOut(duration: 0.2), value: message.wrappedValue)
    }

    /// A modal decision over the screen dimmed by `palette-background` at 80%:
    /// a `bgRaised` panel, `radius-md`, actions at the bottom right with the
    /// main one last. Tapping outside closes it.
    public func kodevDialog<Actions: View>(isPresented: Binding<Bool>, title: String, message: String, @ViewBuilder actions: @escaping () -> Actions) -> some View {
        overlay {
            if isPresented.wrappedValue {
                ZStack {
                    KODevPalette.background.opacity(0.8).ignoresSafeArea()
                        .onTapGesture { isPresented.wrappedValue = false }
                    VStack(alignment: .leading, spacing: KODevSpace.space4) {
                        Text(title).kodevType(KODevType.title).accessibilityAddTraits(.isHeader)
                        Text(message).kodevType(KODevType.body)
                        HStack(spacing: KODevSpace.space3) {
                            Spacer(minLength: 0)
                            actions()
                        }
                        .padding(.top, KODevSpace.space2)
                    }
                    .foregroundStyle(KODevColors.ink)
                    .padding(KODevSpace.space6)
                    .frame(maxWidth: 480)
                    .background(KODevColors.bgRaised, in: RoundedRectangle(cornerRadius: KODevRadius.md))
                    .overlay { RoundedRectangle(cornerRadius: KODevRadius.md).strokeBorder(KODevColors.border) }
                    .padding(KODevSpace.space4)
                    .accessibilityAddTraits(.isModal)
                }
                .transition(.opacity)
            }
        }
        .animation(.easeOut(duration: 0.2), value: isPresented.wrappedValue)
    }

    /// The Dialog pinned to the bottom of the screen, for short choices. Uses
    /// the native sheet in the Design System's colors.
    public func kodevSheet<Content: View>(isPresented: Binding<Bool>, @ViewBuilder content: @escaping () -> Content) -> some View {
        sheet(isPresented: isPresented) {
            content()
                .padding(KODevSpace.space6)
                .frame(maxWidth: .infinity, alignment: .leading)
                .foregroundStyle(KODevColors.ink)
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
                .presentationCornerRadius(KODevRadius.md)
                .presentationBackground(KODevColors.bgRaised)
        }
    }
}
