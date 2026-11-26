package com.koaladevelopments.kodevui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class KODevButtonVariant { Primary, Secondary, Ghost }
enum class KODevButtonSize { Md, Sm }

private data class ButtonColors(val fill: Color, val ink: Color, val frame: Color?)

@Composable
private fun buttonColors(variant: KODevButtonVariant, enabled: Boolean, pressed: Boolean, iconOnly: Boolean): ButtonColors {
    val c = KODevTheme.colors
    if (!enabled) return ButtonColors(c.bgRaised, c.inkMuted, null)
    return when (variant) {
        KODevButtonVariant.Primary -> ButtonColors(if (pressed) c.accentPressed else c.accent, c.onAccent, null)
        KODevButtonVariant.Secondary -> ButtonColors(if (pressed) c.bgRaised else Color.Transparent, c.ink, c.borderStrong)
        KODevButtonVariant.Ghost -> ButtonColors(if (pressed) c.bgRaised else Color.Transparent, if (iconOnly) c.ink else c.inkAccent, null)
    }
}

@Composable
private fun Modifier.buttonSurface(colors: ButtonColors, enabled: Boolean, interaction: MutableInteractionSource, onClick: () -> Unit) =
    this
        .background(colors.fill)
        .then(if (colors.frame != null) Modifier.border(2.dp, colors.frame) else Modifier)
        .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)

/**
 * The Design System's Button: square corners, IBM Plex Mono Bold, 48dp tall.
 * At most one [KODevButtonVariant.Primary] per screen.
 */
@Composable
fun KODevButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KODevButtonVariant = KODevButtonVariant.Primary,
    size: KODevButtonSize = KODevButtonSize.Md,
    block: Boolean = false,
    icon: List<String>? = null,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val colors = buttonColors(variant, enabled, pressed, iconOnly = false)
    val md = size == KODevButtonSize.Md
    Row(
        modifier = modifier
            .then(if (block) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minWidth = 48.dp, minHeight = if (md) 48.dp else 34.dp)
            .buttonSurface(colors, enabled, interaction, onClick)
            .padding(PaddingValues(horizontal = if (md) KODevSpace.space6 else KODevSpace.space4)),
        horizontalArrangement = Arrangement.spacedBy(KODevSpace.space2, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.ink) {
            if (icon != null) KODevIcon(icon)
            Text(
                text = if (md) text else text.uppercase(),
                style = if (md) KODevType.bold else KODevType.caption.copy(fontWeight = FontWeight.Bold),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A square 48dp button with an interface icon and no text. [label] is required: it is what TalkBack says. */
@Composable
fun KODevIconButton(
    icon: List<String>,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KODevButtonVariant = KODevButtonVariant.Ghost,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val colors = buttonColors(variant, enabled, pressed, iconOnly = true)
    Box(
        modifier = modifier
            .size(48.dp)
            .buttonSurface(colors, enabled, interaction, onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        KODevIcon(icon, tint = colors.ink)
    }
}
