import KODevUI
import SwiftUI

/// Every KODevUI token and component on one screen, to review the Design
/// System layer in both themes.
struct DesignSystemGallery: View {
    @State private var dark = true
    @State private var name = ""
    @State private var toast: String?
    @State private var dialog = false
    @State private var sheet = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: KODevSpace.space12) {
                section("Cores") { colors }
                section("Tipografia") { type }
                section("Botões") { buttons }
                section("Ícones") { icons }
                section("Campos") { fields }
                section("Conteúdo") { content }
                section("Sobreposições") { overlays }
            }
            .padding(KODevSpace.space4)
        }
        .background(KODevColors.bg)
        .kodevAppBar(KODevAppBar("KODevUI", actions: {
            KODevIconButton(KODevIcons.more, label: dark ? "Tema claro" : "Tema escuro") { dark.toggle() }
        }))
        .kodevToast($toast)
        .kodevDialog(isPresented: $dialog, title: "Excluir pack?", message: "As 12 figurinhas do pack serão apagadas deste aparelho.") {
            Button("Cancelar") { dialog = false }.buttonStyle(.kodev(.secondary))
            Button("Excluir") { dialog = false }.buttonStyle(.kodev(.primary))
        }
        .kodevSheet(isPresented: $sheet) {
            VStack(alignment: .leading, spacing: KODevSpace.space4) {
                Text("Tipo do pack").kodevType(KODevType.title)
                Text("O tipo fica travado depois de criar o pack.").kodevType(KODevType.body).foregroundStyle(KODevColors.inkMuted)
                Button("Estático") { sheet = false }.buttonStyle(.kodev(.secondary, block: true))
                Button("Animado") { sheet = false }.buttonStyle(.kodev(.secondary, block: true))
            }
        }
        .preferredColorScheme(dark ? .dark : .light)
    }

    private func section(_ title: String, @ViewBuilder content: () -> some View) -> some View {
        VStack(alignment: .leading, spacing: KODevSpace.space4) {
            Text(title.uppercased()).kodevType(KODevType.caption).foregroundStyle(KODevColors.inkMuted)
            content()
        }
    }

    private var colors: some View {
        let tokens: [(String, Color)] = [
            ("bg", KODevColors.bg), ("bg-raised", KODevColors.bgRaised), ("ink", KODevColors.ink),
            ("ink-muted", KODevColors.inkMuted), ("ink-accent", KODevColors.inkAccent), ("accent", KODevColors.accent),
            ("border", KODevColors.border), ("border-strong", KODevColors.borderStrong),
            ("danger", KODevColors.danger), ("success", KODevColors.success),
        ]
        return LazyVGrid(columns: [GridItem(.adaptive(minimum: 96), spacing: KODevSpace.space2)], spacing: KODevSpace.space2) {
            ForEach(tokens, id: \.0) { name, color in
                VStack(alignment: .leading, spacing: KODevSpace.space1) {
                    color.frame(height: 48).overlay { Rectangle().strokeBorder(KODevColors.border) }
                    Text(name).kodevType(KODevType.caption.tracking(0))
                }
            }
        }
    }

    private var type: some View {
        VStack(alignment: .leading, spacing: KODevSpace.space2) {
            Text("Heading").kodevType(KODevType.heading).foregroundStyle(KODevColors.inkAccent)
            Text("Heading small").kodevType(KODevType.headingSmall)
            Text("Title").kodevType(KODevType.title)
            Text("Body: crie packs de figurinhas a partir das suas fotos.").kodevType(KODevType.body)
            Text("CAPTION").kodevType(KODevType.caption).foregroundStyle(KODevColors.inkMuted)
        }
    }

    private var buttons: some View {
        VStack(alignment: .leading, spacing: KODevSpace.space2) {
            Button("Adicionar ao WhatsApp") { toast = "Pack enviado." }.buttonStyle(.kodev(.primary, block: true))
            HStack(spacing: KODevSpace.space2) {
                Button("Secundário") {}.buttonStyle(.kodev(.secondary))
                Button("Ghost") {}.buttonStyle(.kodev(.ghost))
            }
            HStack(spacing: KODevSpace.space2) {
                Button("Pequeno") {}.buttonStyle(.kodev(.secondary, size: .sm))
                Button("Desabilitado") {}.buttonStyle(.kodev(.primary)).disabled(true)
            }
            HStack(spacing: KODevSpace.space2) {
                KODevIconButton(KODevIcons.plus, label: "Adicionar", variant: .primary) {}
                KODevIconButton(KODevIcons.heart, label: "Favoritar", variant: .secondary) {}
                KODevIconButton(KODevIcons.search, label: "Buscar") {}
            }
        }
    }

    private var icons: some View {
        let all = [KODevIcons.arrowLeft, KODevIcons.chevronLeft, KODevIcons.chevronRight, KODevIcons.chevronDown,
                   KODevIcons.menu, KODevIcons.close, KODevIcons.plus, KODevIcons.check, KODevIcons.search,
                   KODevIcons.home, KODevIcons.user, KODevIcons.bell, KODevIcons.heart, KODevIcons.more]
        return LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 7), spacing: KODevSpace.space4) {
            ForEach(all.indices, id: \.self) { KODevIcon(all[$0]) }
        }
    }

    private var fields: some View {
        VStack(alignment: .leading, spacing: KODevSpace.space4) {
            KODevTextField("Nome do pack", text: $name, hint: "Até 128 caracteres.", placeholder: "Meus gatos")
            KODevTextField("Autor", text: .constant(""), error: "Informe quem criou o pack.")
            KODevTextField("Identificador", text: .constant("pack_01")).disabled(true)
        }
    }

    private var content: some View {
        KODevCard {
            VStack(alignment: .leading, spacing: KODevSpace.space2) {
                HStack {
                    KODevTag("Animado", tone: .accent)
                    KODevTag("Rascunho")
                }
                Text("Meus gatos").kodevType(KODevType.title)
                Text("2 de 3 figurinhas mínimas").kodevType(KODevType.body).foregroundStyle(KODevColors.inkMuted)
                HStack {
                    KODevTag("Pronto", tone: .success)
                    KODevTag("Erro", tone: .danger)
                }
            }
        }
    }

    private var overlays: some View {
        VStack(alignment: .leading, spacing: KODevSpace.space2) {
            KODevToast("Figurinha salva.", tone: .success)
            HStack(spacing: KODevSpace.space2) {
                Button("Dialog") { dialog = true }.buttonStyle(.kodev(.secondary))
                Button("Sheet") { sheet = true }.buttonStyle(.kodev(.secondary))
                Button("Toast") { toast = "Mensagem enviada." }.buttonStyle(.kodev(.secondary))
            }
        }
    }
}

#Preview {
    NavigationStack { DesignSystemGallery() }.kodevTheme()
}
