package com.koaladevelopments.stickerstudio.packs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koaladevelopments.kodevui.KODevAppBar
import com.koaladevelopments.kodevui.KODevButton
import com.koaladevelopments.kodevui.KODevButtonSize
import com.koaladevelopments.kodevui.KODevButtonVariant
import com.koaladevelopments.kodevui.KODevDialog
import com.koaladevelopments.kodevui.KODevIcon
import com.koaladevelopments.kodevui.KODevIconButton
import com.koaladevelopments.kodevui.KODevIcons
import com.koaladevelopments.kodevui.KODevSpace
import com.koaladevelopments.kodevui.KODevTag
import com.koaladevelopments.kodevui.KODevTagTone
import com.koaladevelopments.kodevui.KODevTheme
import com.koaladevelopments.kodevui.KODevType
import com.koaladevelopments.stickerstudio.domain.PackKind
import com.koaladevelopments.stickerstudio.domain.Sticker
import com.koaladevelopments.stickerstudio.domain.StickerPack
import com.koaladevelopments.stickerstudio.domain.WhatsAppLimits
import com.koaladevelopments.stickerstudio.domain.validate
import java.util.Collections
import java.util.UUID
import kotlinx.coroutines.launch

/**
 * One pack: its stickers in a grid, with add, reorder and delete, and the
 * pack's own Add to WhatsApp button (WhatsApp forbids an "add all").
 */
@Composable
fun PackDetailScreen(
    repository: PackRepository,
    packId: UUID,
    onEdit: (StickerPack) -> Unit,
    onAddSticker: (StickerPack) -> Unit,
    onAddToWhatsApp: (StickerPack) -> Unit,
    onClose: () -> Unit,
) {
    val c = KODevTheme.colors
    val scope = rememberCoroutineScope()
    val packs by repository.packs.collectAsState()
    val pack = packs.firstOrNull { it.id == packId } ?: return
    var organizing by remember { mutableStateOf(false) }
    var stickerToDelete by remember { mutableStateOf<UUID?>(null) }
    var confirmPackDelete by remember { mutableStateOf(false) }
    val status = PackStatus.of(pack)
    val max = WhatsAppLimits.STICKERS_PER_PACK.last

    fun save(updated: StickerPack) = scope.launch { repository.save(updated) }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        KODevAppBar(
            pack.name,
            leading = { KODevIconButton(KODevIcons.arrowLeft, "Voltar", onClose) },
            actions = { KODevIconButton(KODevIcons.more, "Editar pack", { onEdit(pack) }) },
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(KODevSpace.space4),
            verticalArrangement = Arrangement.spacedBy(KODevSpace.space6),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                    KODevTag(if (pack.kind == PackKind.ANIMATED) "Animado" else "Estático")
                    PackStatusTag(status)
                }
                Text("${pack.stickers.size} de $max figurinhas · ${pack.publisher}", style = KODevType.body, color = c.inkMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FIGURINHAS", style = KODevType.caption, color = c.inkMuted, modifier = Modifier.weight(1f))
                if (pack.stickers.size > 1) {
                    KODevButton(
                        if (organizing) "Pronto" else "Organizar", { organizing = !organizing },
                        variant = KODevButtonVariant.Ghost, size = KODevButtonSize.Sm,
                    )
                }
            }
            // ponytail: plain rows of 3 instead of a lazy grid; a pack has at most 30 stickers.
            val cells: List<Any> = pack.stickers + if (!organizing && pack.stickers.size < max) listOf(AddCell) else emptyList()
            Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                cells.chunked(3).forEachIndexed { row, chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                        chunk.forEachIndexed { col, cell ->
                            val index = row * 3 + col
                            val cellModifier = Modifier.weight(1f).aspectRatio(1f)
                            if (cell is Sticker) {
                                StickerTile(
                                    cell, index, organizing, pack.stickers.size, cellModifier,
                                    onMove = { offset ->
                                        val list = pack.stickers.toMutableList()
                                        Collections.swap(list, index, index + offset)
                                        save(pack.copy(stickers = list))
                                    },
                                    onDelete = { stickerToDelete = cell.id },
                                )
                            } else {
                                Box(
                                    cellModifier
                                        .border(2.dp, c.borderStrong)
                                        .clickable(role = Role.Button) { onAddSticker(pack) }
                                        .semantics { contentDescription = "Adicionar figurinha" },
                                    contentAlignment = Alignment.Center,
                                ) { KODevIcon(KODevIcons.plus) }
                            }
                        }
                        repeat(3 - chunk.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            if (pack.stickers.size >= max) {
                Text(
                    "O pack chegou a $max figurinhas, o máximo do WhatsApp. Exclua uma para adicionar outra.",
                    style = KODevType.caption.copy(letterSpacing = 0.sp), color = c.inkMuted,
                )
            }
            KODevButton("Excluir pack", { confirmPackDelete = true }, variant = KODevButtonVariant.Secondary)
        }
        Column(Modifier.padding(KODevSpace.space4), verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
            KODevButton("Adicionar ao WhatsApp", { onAddToWhatsApp(pack) }, block = true, enabled = status == PackStatus.Ready)
            val issues = pack.validate().size
            val reason = when (status) {
                PackStatus.Ready -> null
                PackStatus.Draft -> "Adicione pelo menos ${WhatsAppLimits.STICKERS_PER_PACK.first} figurinhas para enviar."
                PackStatus.NeedsFixes -> if (issues == 1) "Corrija 1 problema antes de enviar." else "Corrija $issues problemas antes de enviar."
            }
            if (reason != null) Text(reason, style = KODevType.caption.copy(letterSpacing = 0.sp), color = c.inkMuted)
        }
    }

    stickerToDelete?.let { id ->
        KODevDialog("Excluir figurinha?", "Ela sai deste pack e do aparelho.", onDismiss = { stickerToDelete = null }) {
            KODevButton("Cancelar", { stickerToDelete = null }, variant = KODevButtonVariant.Secondary)
            KODevButton("Excluir", {
                stickerToDelete = null
                val rest = pack.stickers.filterNot { it.id == id }
                if (rest.size < 2) organizing = false
                save(pack.copy(stickers = rest))
            })
        }
    }
    if (confirmPackDelete) {
        KODevDialog("Excluir pack?", "As ${pack.stickers.size} figurinhas do pack serão apagadas deste aparelho.", onDismiss = { confirmPackDelete = false }) {
            KODevButton("Cancelar", { confirmPackDelete = false }, variant = KODevButtonVariant.Secondary)
            KODevButton("Excluir", {
                confirmPackDelete = false
                scope.launch {
                    repository.delete(pack.id)
                    onClose()
                }
            })
        }
    }
}

private object AddCell

/** The list and detail screens' state tag. */
@Composable
fun PackStatusTag(status: PackStatus) = when (status) {
    PackStatus.Draft -> KODevTag("Rascunho")
    PackStatus.NeedsFixes -> KODevTag("Corrigir", tone = KODevTagTone.Danger)
    PackStatus.Ready -> KODevTag("Pronto", tone = KODevTagTone.Success)
}

@Composable
private fun StickerTile(
    sticker: Sticker,
    index: Int,
    organizing: Boolean,
    count: Int,
    modifier: Modifier,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val c = KODevTheme.colors
    // ponytail: emoji placeholder until the core storage exposes sticker files to load thumbnails from (#3).
    Box(modifier.background(c.bgRaised).semantics { contentDescription = "Figurinha ${index + 1}" }) {
        Text(sticker.emojis.firstOrNull() ?: "", fontSize = 40.sp, modifier = Modifier.align(Alignment.Center))
        Text("${index + 1}", style = KODevType.caption.copy(letterSpacing = 0.sp), color = c.inkMuted, modifier = Modifier.padding(KODevSpace.space1))
        if (organizing) {
            Column(Modifier.fillMaxSize().background(c.bg.copy(alpha = 0.6f))) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    KODevIconButton(KODevIcons.close, "Excluir figurinha ${index + 1}", onDelete)
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    KODevIconButton(KODevIcons.chevronLeft, "Mover para trás", { onMove(-1) }, enabled = index > 0)
                    KODevIconButton(KODevIcons.chevronRight, "Mover para frente", { onMove(1) }, enabled = index < count - 1)
                }
            }
        }
    }
}
