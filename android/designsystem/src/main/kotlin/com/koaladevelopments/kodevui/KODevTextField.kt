package com.koaladevelopments.kodevui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A text field with the label on top, an optional hint and an error message, in
 * the same 2dp square frame as the secondary button. [label] names the data
 * ("Nome do pack"); [error] says how to fix it and replaces the hint.
 */
@Composable
fun KODevTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    error: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
) {
    val c = KODevTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val frame = when {
        error != null -> c.danger
        focused -> c.focusRing
        else -> c.borderStrong
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(KODevSpace.space2)) {
        Text(label.uppercase(), style = KODevType.caption.copy(fontWeight = FontWeight.Bold), color = c.ink)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            interactionSource = interaction,
            textStyle = KODevType.body.copy(color = if (enabled) c.ink else c.inkMuted),
            cursorBrush = SolidColor(c.focusRing),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (error != null) Modifier.semantics { error(error) } else Modifier),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .heightIn(min = 48.dp)
                        .then(if (enabled) Modifier.border(2.dp, frame) else Modifier.background(c.bgRaised))
                        .padding(horizontal = KODevSpace.space4),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) Text(placeholder, style = KODevType.body, color = c.inkMuted)
                    inner()
                }
            },
        )
        val message = error ?: hint
        if (message != null) {
            Text(message, style = KODevType.caption.copy(letterSpacing = 0.sp), color = if (error != null) c.danger else c.inkMuted)
        }
    }
}
