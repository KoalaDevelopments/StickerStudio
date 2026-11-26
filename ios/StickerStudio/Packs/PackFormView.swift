import KODevUI
import StickerDomain
import SwiftUI

/// Creates a pack, or renames one when given `pack`. The type is chosen once:
/// WhatsApp forbids mixing static and animated stickers in a pack.
struct PackFormView: View {
    let repository: PackRepository
    let pack: StickerPack?
    var onDone: () -> Void

    @State private var name: String
    @State private var publisher: String
    @State private var kind: PackKind
    @State private var submitted = false

    init(repository: PackRepository, pack: StickerPack? = nil, onDone: @escaping () -> Void) {
        self.repository = repository
        self.pack = pack
        self.onDone = onDone
        _name = State(initialValue: pack?.name ?? "")
        _publisher = State(initialValue: pack?.publisher ?? "")
        _kind = State(initialValue: pack?.kind ?? .static)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: KODevSpace.space6) {
                KODevTextField("Nome do pack", text: $name, hint: hint(name), error: submitted ? error(name, empty: "Informe o nome do pack.") : nil, placeholder: "Meus gatos")
                KODevTextField("Autor", text: $publisher, hint: hint(publisher), error: submitted ? error(publisher, empty: "Informe quem criou o pack.") : nil, placeholder: "Seu nome")
                VStack(alignment: .leading, spacing: KODevSpace.space1) {
                    Text("TIPO DO PACK").kodevType(KODevType.captionBold)
                    KODevRadio("Estático: fotos e imagens", isSelected: kind == .static) { kind = .static }
                    KODevRadio("Animado: vídeos e GIFs", isSelected: kind == .animated) { kind = .animated }
                    Text(pack == nil ? "Não dá para trocar depois: o WhatsApp não mistura os dois tipos no mesmo pack." : "O tipo não muda depois de criar o pack.")
                        .kodevType(KODevType.caption.tracking(0))
                        .foregroundStyle(KODevColors.inkMuted)
                }
                .disabled(pack != nil)
            }
            .padding(KODevSpace.space4)
        }
        .safeAreaInset(edge: .bottom) {
            Button(pack == nil ? "Criar pack" : "Salvar", action: save)
                .buttonStyle(.kodev(.primary, block: true))
                .padding(KODevSpace.space4)
                .background(KODevColors.bg)
        }
        .background(KODevColors.bg)
        .kodevAppBar(KODevAppBar(pack == nil ? "Novo pack" : "Editar pack", leading: {
            KODevIconButton(KODevIcons.close, label: "Cancelar", action: onDone)
        }))
    }

    private func hint(_ text: String) -> String {
        "\(text.utf16.count) de \(WhatsAppLimits.textLength.upperBound) caracteres."
    }

    private func error(_ text: String, empty: String) -> String? {
        let length = text.trimmingCharacters(in: .whitespaces).utf16.count
        if length == 0 { return empty }
        if length > WhatsAppLimits.textLength.upperBound {
            return "Use até \(WhatsAppLimits.textLength.upperBound) caracteres. Agora são \(length)."
        }
        return nil
    }

    private func save() {
        submitted = true
        guard error(name, empty: "") == nil, error(publisher, empty: "") == nil else { return }
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let trimmedPublisher = publisher.trimmingCharacters(in: .whitespaces)
        var saved = pack ?? StickerPack(name: trimmedName, publisher: trimmedPublisher, kind: kind)
        saved.name = trimmedName
        saved.publisher = trimmedPublisher
        Task {
            await repository.save(saved)
            onDone()
        }
    }
}

#Preview {
    NavigationStack { PackFormView(repository: InMemoryPackRepository(), onDone: {}) }.kodevTheme()
}
