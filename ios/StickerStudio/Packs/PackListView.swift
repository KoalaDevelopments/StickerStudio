import KODevUI
import StickerDomain
import SwiftUI

/// The packs limit while WhatsApp's enforcement is unverified (D1, #13).
let maxPacks = 10

/// Where a pack stands, as the list shows it.
enum PackStatus: Equatable {
    /// Fewer than the minimum stickers.
    case draft
    /// Enough stickers but WhatsApp rules broken (the detail screen lists them).
    case needsFixes
    case ready

    init(_ pack: StickerPack) {
        if pack.stickers.count < WhatsAppLimits.stickersPerPack.lowerBound {
            self = .draft
        } else {
            self = pack.validate().isEmpty ? .ready : .needsFixes
        }
    }
}

/// Home screen: the user's packs, with their state, and the way to create one.
struct PackListView: View {
    let repository: PackRepository
    var onCreate: () -> Void = {}
    var onOpen: (StickerPack) -> Void = { _ in }
    var onOpenGallery: (() -> Void)?

    @State private var packs: [StickerPack]?

    var body: some View {
        Group {
            switch packs {
            case nil: Color.clear
            case let packs? where packs.isEmpty: emptyState
            case let packs?: list(packs)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(KODevColors.bg)
        .kodevAppBar(KODevAppBar("Meus packs", actions: {
            if let onOpenGallery {
                KODevIconButton(KODevIcons.more, label: "Galeria do Design System", action: onOpenGallery)
            }
            if let packs, !packs.isEmpty {
                KODevIconButton(KODevIcons.plus, label: "Criar pack", action: onCreate)
                    .disabled(packs.count >= maxPacks)
            }
        }))
        .onAppear { Task { packs = await repository.packs() } }
    }

    private var emptyState: some View {
        VStack(alignment: .leading, spacing: KODevSpace.space4) {
            Text("Nenhum pack ainda").kodevType(KODevType.headingSmall).foregroundStyle(KODevColors.inkAccent)
            Text("Crie um pack e adicione pelo menos 3 figurinhas feitas das suas fotos, vídeos ou GIFs.")
                .kodevType(KODevType.body)
                .foregroundStyle(KODevColors.inkMuted)
            Button("Criar pack", action: onCreate).buttonStyle(.kodev(.primary, block: true))
        }
        .padding(KODevSpace.space4)
        .frame(maxHeight: .infinity, alignment: .center)
    }

    private func list(_ packs: [StickerPack]) -> some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: KODevSpace.space4) {
                ForEach(packs) { pack in
                    Button { onOpen(pack) } label: { PackRow(pack: pack) }
                        .buttonStyle(.plain)
                }
                if packs.count >= maxPacks {
                    Text("Você chegou ao limite de \(maxPacks) packs. Exclua um para criar outro.")
                        .kodevType(KODevType.caption.tracking(0))
                        .foregroundStyle(KODevColors.inkMuted)
                }
            }
            .padding(KODevSpace.space4)
        }
    }
}

private struct PackRow: View {
    let pack: StickerPack

    var body: some View {
        KODevCard {
            VStack(alignment: .leading, spacing: KODevSpace.space2) {
                HStack(spacing: KODevSpace.space2) {
                    KODevTag(pack.kind == .animated ? "Animado" : "Estático")
                    status
                }
                Text(pack.name).kodevType(KODevType.title).lineLimit(2)
                Text("\(pack.stickers.count) de \(WhatsAppLimits.stickersPerPack.upperBound) figurinhas · \(pack.publisher)")
                    .kodevType(KODevType.body)
                    .foregroundStyle(KODevColors.inkMuted)
            }
        }
        .accessibilityElement(children: .combine)
    }

    private var status: some View { PackStatusTag(status: PackStatus(pack)) }
}

#Preview("Com packs") {
    NavigationStack { PackListView(repository: InMemoryPackRepository.sample) }.kodevTheme()
}

#Preview("Vazio") {
    NavigationStack { PackListView(repository: InMemoryPackRepository()) }.kodevTheme()
}
