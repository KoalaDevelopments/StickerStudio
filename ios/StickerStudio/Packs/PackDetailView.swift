import KODevUI
import StickerDomain
import SwiftUI

/// One pack: its stickers in a grid, with add, reorder and delete, and the
/// pack's own Add to WhatsApp button (WhatsApp forbids an "add all").
struct PackDetailView: View {
    let repository: PackRepository
    let packID: StickerPack.ID
    var onEdit: (StickerPack) -> Void = { _ in }
    var onAddSticker: (StickerPack) -> Void = { _ in }
    var onAddToWhatsApp: (StickerPack) -> Void = { _ in }
    var onClose: () -> Void

    @State private var pack: StickerPack?
    @State private var organizing = false
    @State private var stickerToDelete: Sticker.ID?
    @State private var confirmPackDelete = false

    var body: some View {
        Group {
            if let pack { content(pack) } else { Color.clear }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(KODevColors.bg)
        .kodevAppBar(KODevAppBar(pack?.name ?? "", leading: {
            KODevIconButton(KODevIcons.arrowLeft, label: "Voltar", action: onClose)
        }, actions: {
            if let pack {
                KODevIconButton(KODevIcons.more, label: "Editar pack") { onEdit(pack) }
            }
        }))
        .kodevDialog(isPresented: Binding(get: { stickerToDelete != nil }, set: { if !$0 { stickerToDelete = nil } }),
                     title: "Excluir figurinha?", message: "Ela sai deste pack e do aparelho.") {
            Button("Cancelar") { stickerToDelete = nil }.buttonStyle(.kodev(.secondary))
            Button("Excluir") { deleteSticker() }.buttonStyle(.kodev(.primary))
        }
        .kodevDialog(isPresented: $confirmPackDelete, title: "Excluir pack?",
                     message: "As \(pack?.stickers.count ?? 0) figurinhas do pack serão apagadas deste aparelho.") {
            Button("Cancelar") { confirmPackDelete = false }.buttonStyle(.kodev(.secondary))
            Button("Excluir") { deletePack() }.buttonStyle(.kodev(.primary))
        }
        .onAppear { Task { await reload() } }
    }

    private func content(_ pack: StickerPack) -> some View {
        let status = PackStatus(pack)
        let max = WhatsAppLimits.stickersPerPack.upperBound
        return ScrollView {
            VStack(alignment: .leading, spacing: KODevSpace.space6) {
                VStack(alignment: .leading, spacing: KODevSpace.space2) {
                    HStack(spacing: KODevSpace.space2) {
                        KODevTag(pack.kind == .animated ? "Animado" : "Estático")
                        PackStatusTag(status: status)
                    }
                    Text("\(pack.stickers.count) de \(max) figurinhas · \(pack.publisher)")
                        .kodevType(KODevType.body)
                        .foregroundStyle(KODevColors.inkMuted)
                }

                HStack {
                    Text("FIGURINHAS").kodevType(KODevType.caption).foregroundStyle(KODevColors.inkMuted)
                    Spacer()
                    if pack.stickers.count > 1 {
                        Button(organizing ? "Pronto" : "Organizar") { organizing.toggle() }
                            .buttonStyle(.kodev(.ghost, size: .sm))
                    }
                }

                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: KODevSpace.space2), count: 3), spacing: KODevSpace.space2) {
                    ForEach(Array(pack.stickers.enumerated()), id: \.element.id) { index, sticker in
                        StickerTile(sticker: sticker, index: index, organizing: organizing,
                                    canMoveLeft: index > 0, canMoveRight: index < pack.stickers.count - 1,
                                    onMove: { move(from: index, by: $0) },
                                    onDelete: { stickerToDelete = sticker.id })
                    }
                    if !organizing && pack.stickers.count < max {
                        Button { onAddSticker(pack) } label: {
                            Color.clear
                                .aspectRatio(1, contentMode: .fit)
                                .overlay { KODevIcon(KODevIcons.plus) }
                                .overlay { Rectangle().strokeBorder(KODevColors.borderStrong, lineWidth: 2) }
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Adicionar figurinha")
                    }
                }
                if pack.stickers.count >= max {
                    Text("O pack chegou a \(max) figurinhas, o máximo do WhatsApp. Exclua uma para adicionar outra.")
                        .kodevType(KODevType.caption.tracking(0))
                        .foregroundStyle(KODevColors.inkMuted)
                }

                Button("Excluir pack") { confirmPackDelete = true }
                    .buttonStyle(.kodev(.secondary))
            }
            .padding(KODevSpace.space4)
        }
        .safeAreaInset(edge: .bottom) {
            VStack(alignment: .leading, spacing: KODevSpace.space2) {
                Button("Adicionar ao WhatsApp") { onAddToWhatsApp(pack) }
                    .buttonStyle(.kodev(.primary, block: true))
                    .disabled(status != .ready)
                if let reason = blockedReason(pack, status) {
                    Text(reason).kodevType(KODevType.caption.tracking(0)).foregroundStyle(KODevColors.inkMuted)
                }
            }
            .padding(KODevSpace.space4)
            .background(KODevColors.bg)
        }
    }

    private func blockedReason(_ pack: StickerPack, _ status: PackStatus) -> String? {
        switch status {
        case .ready: nil
        case .draft: "Adicione pelo menos \(WhatsAppLimits.stickersPerPack.lowerBound) figurinhas para enviar."
        case .needsFixes:
            pack.validate().count == 1 ? "Corrija 1 problema antes de enviar." : "Corrija \(pack.validate().count) problemas antes de enviar."
        }
    }

    private func reload() async {
        pack = await repository.packs().first { $0.id == packID }
        if pack == nil { onClose() }
    }

    private func move(from index: Int, by offset: Int) {
        guard var pack else { return }
        pack.stickers.swapAt(index, index + offset)
        save(pack)
    }

    private func deleteSticker() {
        guard var pack, let id = stickerToDelete else { return }
        pack.stickers.removeAll { $0.id == id }
        stickerToDelete = nil
        if pack.stickers.count < 2 { organizing = false }
        save(pack)
    }

    private func deletePack() {
        confirmPackDelete = false
        Task {
            await repository.delete(id: packID)
            onClose()
        }
    }

    private func save(_ updated: StickerPack) {
        pack = updated
        Task { await repository.save(updated) }
    }
}

/// The list and detail screens' state tag.
struct PackStatusTag: View {
    let status: PackStatus

    var body: some View {
        switch status {
        case .draft: KODevTag("Rascunho")
        case .needsFixes: KODevTag("Corrigir", tone: .danger)
        case .ready: KODevTag("Pronto", tone: .success)
        }
    }
}

private struct StickerTile: View {
    let sticker: Sticker
    let index: Int
    let organizing: Bool
    let canMoveLeft: Bool
    let canMoveRight: Bool
    let onMove: (Int) -> Void
    let onDelete: () -> Void

    var body: some View {
        // ponytail: emoji placeholder until the core storage exposes sticker
        // files to load thumbnails from (#3).
        KODevColors.bgRaised
            .aspectRatio(1, contentMode: .fit)
            .overlay { Text(sticker.emojis.first ?? "").font(.system(size: 40)) }
            .overlay(alignment: .topLeading) {
                Text("\(index + 1)").kodevType(KODevType.caption.tracking(0)).foregroundStyle(KODevColors.inkMuted).padding(KODevSpace.space1)
            }
            .overlay {
                if organizing {
                    VStack(spacing: 0) {
                        HStack {
                            Spacer()
                            KODevIconButton(KODevIcons.close, label: "Excluir figurinha \(index + 1)", action: onDelete)
                        }
                        Spacer()
                        HStack {
                            KODevIconButton(KODevIcons.chevronLeft, label: "Mover para trás") { onMove(-1) }.disabled(!canMoveLeft)
                            Spacer()
                            KODevIconButton(KODevIcons.chevronRight, label: "Mover para frente") { onMove(1) }.disabled(!canMoveRight)
                        }
                    }
                    .background(KODevColors.bg.opacity(0.6))
                }
            }
            .accessibilityElement(children: organizing ? .contain : .combine)
            .accessibilityLabel("Figurinha \(index + 1)")
    }
}

#Preview {
    NavigationStack {
        PackDetailView(repository: InMemoryPackRepository.sample, packID: UUID(), onClose: {})
    }
    .kodevTheme()
}
