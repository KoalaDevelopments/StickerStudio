package com.koaladevelopments.stickerstudio.packs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.koaladevelopments.kodevui.KODevAppBar
import com.koaladevelopments.kodevui.KODevButton
import com.koaladevelopments.kodevui.KODevIconButton
import com.koaladevelopments.kodevui.KODevIcons
import com.koaladevelopments.kodevui.KODevRadio
import com.koaladevelopments.kodevui.KODevSpace
import com.koaladevelopments.kodevui.KODevTextField
import com.koaladevelopments.kodevui.KODevTheme
import com.koaladevelopments.kodevui.KODevType
import com.koaladevelopments.stickerstudio.domain.PackKind
import com.koaladevelopments.stickerstudio.domain.StickerPack
import com.koaladevelopments.stickerstudio.domain.WhatsAppLimits
import kotlinx.coroutines.launch

/**
 * Creates a pack, or renames one when given [pack]. The type is chosen once:
 * WhatsApp forbids mixing static and animated stickers in a pack.
 */
@Composable
fun PackFormScreen(repository: PackRepository, pack: StickerPack?, onDone: () -> Unit) {
    val c = KODevTheme.colors
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(pack?.name ?: "") }
    var publisher by rememberSaveable { mutableStateOf(pack?.publisher ?: "") }
    var kind by rememberSaveable { mutableStateOf(pack?.kind ?: PackKind.STATIC) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val max = WhatsAppLimits.TEXT_LENGTH.last

    fun hint(text: String) = "${text.length} de $max caracteres."
    fun error(text: String, empty: String): String? {
        val length = text.trim().length
        return when {
            length == 0 -> empty
            length > max -> "Use até $max caracteres. Agora são $length."
            else -> null
        }
    }

    Column(Modifier.fillMaxSize().background(c.bg).imePadding()) {
        KODevAppBar(
            if (pack == null) "Novo pack" else "Editar pack",
            leading = { KODevIconButton(KODevIcons.close, "Cancelar", onDone) },
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(KODevSpace.space4),
            verticalArrangement = Arrangement.spacedBy(KODevSpace.space6),
        ) {
            KODevTextField(
                "Nome do pack", name, { name = it }, hint = hint(name),
                error = if (submitted) error(name, "Informe o nome do pack.") else null, placeholder = "Meus gatos",
            )
            KODevTextField(
                "Autor", publisher, { publisher = it }, hint = hint(publisher),
                error = if (submitted) error(publisher, "Informe quem criou o pack.") else null, placeholder = "Seu nome",
            )
            Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space1)) {
                Text("TIPO DO PACK", style = KODevType.caption.copy(fontWeight = FontWeight.Bold))
                KODevRadio("Estático: fotos e imagens", kind == PackKind.STATIC, { kind = PackKind.STATIC }, enabled = pack == null)
                KODevRadio("Animado: vídeos e GIFs", kind == PackKind.ANIMATED, { kind = PackKind.ANIMATED }, enabled = pack == null)
                Text(
                    if (pack == null) "Não dá para trocar depois: o WhatsApp não mistura os dois tipos no mesmo pack."
                    else "O tipo não muda depois de criar o pack.",
                    style = KODevType.caption.copy(letterSpacing = 0.sp),
                    color = c.inkMuted,
                )
            }
        }
        KODevButton(
            if (pack == null) "Criar pack" else "Salvar",
            onClick = {
                submitted = true
                if (error(name, "") == null && error(publisher, "") == null) {
                    val saved = (pack ?: StickerPack(name = name.trim(), publisher = publisher.trim(), kind = kind))
                        .copy(name = name.trim(), publisher = publisher.trim())
                    scope.launch {
                        repository.save(saved)
                        onDone()
                    }
                }
            },
            block = true,
            modifier = Modifier.padding(KODevSpace.space4),
        )
    }
}
