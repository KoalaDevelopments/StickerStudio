package com.koaladevelopments.kodevui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class KODevToastTone { Neutral, Success, Danger }

/**
 * A short feedback message after an action: a bgRaised strip with a
 * borderStrong line and a 4dp bar on the left in the tone's color. Show it
 * above the bottom bar with 16dp margin, one at a time; hide it after 4s.
 */
@Composable
fun KODevToast(text: String, modifier: Modifier = Modifier, tone: KODevToastTone = KODevToastTone.Neutral) {
    val c = KODevTheme.colors
    val bar = when (tone) {
        KODevToastTone.Neutral -> c.accent
        KODevToastTone.Success -> c.success
        KODevToastTone.Danger -> c.danger
    }
    Row(
        modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .height(IntrinsicSize.Min)
            .heightIn(min = 56.dp)
            .background(c.bgRaised)
            .border(1.dp, c.borderStrong)
            .semantics { liveRegion = if (tone == KODevToastTone.Danger) LiveRegionMode.Assertive else LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(bar))
        Text(text, style = KODevType.body, color = c.ink, modifier = Modifier.padding(KODevSpace.space4))
    }
}

/**
 * A modal decision over the screen dimmed by palette-background at 80%: a
 * bgRaised panel with radius-md, the actions at the bottom right with the main
 * one last. Back and tapping outside call [onDismiss].
 */
@Composable
fun KODevDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    val c = KODevTheme.colors
    val shape = RoundedCornerShape(KODevRadius.md)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .padding(KODevSpace.space4)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .background(c.bgRaised, shape)
                .border(1.dp, c.border, shape)
                .padding(KODevSpace.space6),
            verticalArrangement = Arrangement.spacedBy(KODevSpace.space4),
        ) {
            Text(title, style = KODevType.title, color = c.ink, modifier = Modifier.semantics { heading() })
            Text(message, style = KODevType.body, color = c.ink)
            Row(
                Modifier.fillMaxWidth().padding(top = KODevSpace.space2),
                horizontalArrangement = Arrangement.spacedBy(KODevSpace.space3, Alignment.End),
                content = actions,
            )
        }
    }
}

/** The Dialog pinned to the bottom of the screen, for short choices on a phone. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KODevSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = KODevTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = KODevRadius.md, topEnd = KODevRadius.md),
        containerColor = c.bgRaised,
        contentColor = c.ink,
        scrimColor = KODevPalette.background.copy(alpha = 0.8f),
        tonalElevation = 0.dp,
        dragHandle = { Box(Modifier.padding(top = KODevSpace.space4).size(32.dp, 4.dp).background(c.borderStrong)) },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(KODevSpace.space6),
            verticalArrangement = Arrangement.spacedBy(KODevSpace.space4),
            content = content,
        )
    }
}
