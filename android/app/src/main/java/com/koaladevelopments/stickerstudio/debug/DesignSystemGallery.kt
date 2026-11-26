package com.koaladevelopments.stickerstudio.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koaladevelopments.kodevui.KODevAppBar
import com.koaladevelopments.kodevui.KODevButton
import com.koaladevelopments.kodevui.KODevButtonSize
import com.koaladevelopments.kodevui.KODevButtonVariant
import com.koaladevelopments.kodevui.KODevCard
import com.koaladevelopments.kodevui.KODevDialog
import com.koaladevelopments.kodevui.KODevIcon
import com.koaladevelopments.kodevui.KODevIconButton
import com.koaladevelopments.kodevui.KODevIcons
import com.koaladevelopments.kodevui.KODevSheet
import com.koaladevelopments.kodevui.KODevSpace
import com.koaladevelopments.kodevui.KODevTag
import com.koaladevelopments.kodevui.KODevTagTone
import com.koaladevelopments.kodevui.KODevTextField
import com.koaladevelopments.kodevui.KODevTheme
import com.koaladevelopments.kodevui.KODevToast
import com.koaladevelopments.kodevui.KODevToastTone
import com.koaladevelopments.kodevui.KODevType
import kotlinx.coroutines.delay

/** Every :designsystem token and component on one screen, to review it in both themes. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignSystemGallery(dark: Boolean, onToggleTheme: () -> Unit, onBack: () -> Unit) {
    val c = KODevTheme.colors
    var name by remember { mutableStateOf("") }
    var toast by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf(false) }
    var sheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Column {
            KODevAppBar("KODevUI", leading = { KODevIconButton(KODevIcons.arrowLeft, "Voltar", onBack) }, actions = {
                KODevIconButton(KODevIcons.more, if (dark) "Tema claro" else "Tema escuro", onToggleTheme)
            })
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(KODevSpace.space4),
                verticalArrangement = Arrangement.spacedBy(KODevSpace.space12),
            ) {
                Section("Cores") {
                    val tokens = listOf(
                        "bg" to c.bg, "bg-raised" to c.bgRaised, "ink" to c.ink, "ink-muted" to c.inkMuted,
                        "ink-accent" to c.inkAccent, "accent" to c.accent, "border" to c.border,
                        "border-strong" to c.borderStrong, "danger" to c.danger, "success" to c.success,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2), verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                        tokens.forEach { (label, color) -> Swatch(label, color) }
                    }
                }
                Section("Tipografia") {
                    Text("Heading", style = KODevType.heading, color = c.inkAccent)
                    Text("Heading small", style = KODevType.headingSmall)
                    Text("Title", style = KODevType.title)
                    Text("Body: crie packs de figurinhas a partir das suas fotos.", style = KODevType.body)
                    Text("CAPTION", style = KODevType.caption, color = c.inkMuted)
                }
                Section("Botões") {
                    KODevButton("Adicionar ao WhatsApp", { toast = "Pack enviado." }, block = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                        KODevButton("Secundário", {}, variant = KODevButtonVariant.Secondary)
                        KODevButton("Ghost", {}, variant = KODevButtonVariant.Ghost)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                        KODevButton("Pequeno", {}, variant = KODevButtonVariant.Secondary, size = KODevButtonSize.Sm)
                        KODevButton("Desabilitado", {}, enabled = false)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                        KODevIconButton(KODevIcons.plus, "Adicionar", {}, variant = KODevButtonVariant.Primary)
                        KODevIconButton(KODevIcons.heart, "Favoritar", {}, variant = KODevButtonVariant.Secondary)
                        KODevIconButton(KODevIcons.search, "Buscar", {})
                    }
                }
                Section("Ícones") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space6), verticalArrangement = Arrangement.spacedBy(KODevSpace.space4)) {
                        KODevIcons.all.forEach { KODevIcon(it) }
                    }
                }
                Section("Campos") {
                    KODevTextField("Nome do pack", name, { name = it }, hint = "Até 128 caracteres.", placeholder = "Meus gatos")
                    KODevTextField("Autor", "", {}, error = "Informe quem criou o pack.")
                    KODevTextField("Identificador", "pack_01", {}, enabled = false)
                }
                Section("Conteúdo") {
                    KODevCard {
                        Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                                KODevTag("Animado", tone = KODevTagTone.Accent)
                                KODevTag("Rascunho")
                            }
                            Text("Meus gatos", style = KODevType.title)
                            Text("2 de 3 figurinhas mínimas", style = KODevType.body, color = c.inkMuted)
                            Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                                KODevTag("Pronto", tone = KODevTagTone.Success)
                                KODevTag("Erro", tone = KODevTagTone.Danger)
                            }
                        }
                    }
                }
                Section("Sobreposições") {
                    KODevToast("Figurinha salva.", tone = KODevToastTone.Success)
                    Row(horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
                        KODevButton("Dialog", { dialog = true }, variant = KODevButtonVariant.Secondary)
                        KODevButton("Sheet", { sheet = true }, variant = KODevButtonVariant.Secondary)
                        KODevButton("Toast", { toast = "Mensagem enviada." }, variant = KODevButtonVariant.Secondary)
                    }
                }
            }
        }

        toast?.let { text ->
            LaunchedEffect(text) {
                delay(4000)
                toast = null
            }
            KODevToast(text, Modifier.align(Alignment.BottomCenter).padding(KODevSpace.space4))
        }
    }

    if (dialog) {
        KODevDialog("Excluir pack?", "As 12 figurinhas do pack serão apagadas deste aparelho.", onDismiss = { dialog = false }) {
            KODevButton("Cancelar", { dialog = false }, variant = KODevButtonVariant.Secondary)
            KODevButton("Excluir", { dialog = false })
        }
    }
    if (sheet) {
        KODevSheet(onDismiss = { sheet = false }) {
            Text("Tipo do pack", style = KODevType.title)
            Text("O tipo fica travado depois de criar o pack.", style = KODevType.body, color = c.inkMuted)
            KODevButton("Estático", { sheet = false }, variant = KODevButtonVariant.Secondary, block = true)
            KODevButton("Animado", { sheet = false }, variant = KODevButtonVariant.Secondary, block = true)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(KODevSpace.space4)) {
        Text(title.uppercase(), style = KODevType.caption, color = KODevTheme.colors.inkMuted)
        content()
    }
}

@Composable
private fun Swatch(label: String, color: Color) {
    Column(Modifier.width(96.dp), verticalArrangement = Arrangement.spacedBy(KODevSpace.space1)) {
        Box(Modifier.fillMaxWidth().height(48.dp).background(color).border(1.dp, KODevTheme.colors.border))
        Text(label, style = KODevType.caption.copy(letterSpacing = 0.sp))
    }
}
