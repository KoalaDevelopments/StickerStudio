package com.koaladevelopments.stickerstudio.packs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.koaladevelopments.kodevui.KODevAppBar
import com.koaladevelopments.kodevui.KODevButton
import com.koaladevelopments.kodevui.KODevCard
import com.koaladevelopments.kodevui.KODevIconButton
import com.koaladevelopments.kodevui.KODevIcons
import com.koaladevelopments.kodevui.KODevSpace
import com.koaladevelopments.kodevui.KODevTag
import com.koaladevelopments.kodevui.KODevTagTone
import com.koaladevelopments.kodevui.KODevTheme
import com.koaladevelopments.kodevui.KODevType
import com.koaladevelopments.stickerstudio.domain.PackKind
import com.koaladevelopments.stickerstudio.domain.StickerPack
import com.koaladevelopments.stickerstudio.domain.WhatsAppLimits
import com.koaladevelopments.stickerstudio.domain.validate

/** The packs limit while WhatsApp's enforcement is unverified (D1, #13). */
const val MAX_PACKS = 10

/** Where a pack stands, as the list shows it. */
enum class PackStatus {
    /** Fewer than the minimum stickers. */
    Draft,
    /** Enough stickers but WhatsApp rules broken (the detail screen lists them). */
    NeedsFixes,
    Ready;

    companion object {
        fun of(pack: StickerPack) = when {
            pack.stickers.size < WhatsAppLimits.STICKERS_PER_PACK.first -> Draft
            pack.validate().isEmpty() -> Ready
            else -> NeedsFixes
        }
    }
}

/** Home screen: the user's packs, with their state, and the way to create one. */
@Composable
fun PackListScreen(
    repository: PackRepository,
    onCreate: () -> Unit,
    onOpen: (StickerPack) -> Unit,
    onOpenGallery: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val c = KODevTheme.colors
    val packs by repository.packs.collectAsState()
    Column(modifier.fillMaxSize().background(c.bg)) {
        KODevAppBar("Meus packs", actions = {
            if (onOpenGallery != null) KODevIconButton(KODevIcons.more, "Galeria do Design System", onOpenGallery)
            if (packs.isNotEmpty()) KODevIconButton(KODevIcons.plus, "Criar pack", onCreate, enabled = packs.size < MAX_PACKS)
        })
        if (packs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(KODevSpace.space4), contentAlignment = Alignment.Center) {
                Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space4)) {
                    Text("Nenhum pack ainda", style = KODevType.headingSmall, color = c.inkAccent)
                    Text(
                        "Crie um pack e adicione pelo menos 3 figurinhas feitas das suas fotos, vídeos ou GIFs.",
                        style = KODevType.body,
                        color = c.inkMuted,
                    )
                    KODevButton("Criar pack", onCreate, block = true)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(KODevSpace.space4),
                verticalArrangement = Arrangement.spacedBy(KODevSpace.space4),
            ) {
                items(packs, key = { it.id }) { pack -> PackRow(pack, onClick = { onOpen(pack) }) }
                if (packs.size >= MAX_PACKS) {
                    item {
                        Text(
                            "Você chegou ao limite de $MAX_PACKS packs. Exclua um para criar outro.",
                            style = KODevType.caption.copy(letterSpacing = 0.sp),
                            color = c.inkMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackRow(pack: StickerPack, onClick: () -> Unit) {
    val c = KODevTheme.colors
    KODevCard(Modifier.clickable(role = Role.Button, onClick = onClick).semantics(mergeDescendants = true) {}) {
        Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                KODevTag(if (pack.kind == PackKind.ANIMATED) "Animado" else "Estático")
                when (PackStatus.of(pack)) {
                    PackStatus.Draft -> KODevTag("Rascunho")
                    PackStatus.NeedsFixes -> KODevTag("Corrigir", tone = KODevTagTone.Danger)
                    PackStatus.Ready -> KODevTag("Pronto", tone = KODevTagTone.Success)
                }
            }
            Text(pack.name, style = KODevType.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${pack.stickers.size} de ${WhatsAppLimits.STICKERS_PER_PACK.last} figurinhas · ${pack.publisher}",
                style = KODevType.body,
                color = c.inkMuted,
            )
        }
    }
}
