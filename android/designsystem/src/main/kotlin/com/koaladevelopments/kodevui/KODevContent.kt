package com.koaladevelopments.kodevui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** The top bar of a screen, 56dp: optional control on the left, the title (never centered), up to three actions. */
@Composable
fun KODevAppBar(
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = KODevTheme.colors
    Column(modifier.background(c.bg)) {
        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) leading() else Spacer(Modifier.width(KODevSpace.space4))
            Text(
                title,
                style = KODevType.title,
                color = c.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            actions()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
    }
}

/** A panel that groups content: bgRaised, a border line, radius-md corners and space-6 padding. No shadow. */
@Composable
fun KODevCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val c = KODevTheme.colors
    val shape = RoundedCornerShape(KODevRadius.md)
    Box(
        modifier
            .fillMaxWidth()
            .background(c.bgRaised, shape)
            .border(1.dp, c.border, shape)
            .padding(KODevSpace.space6),
    ) {
        CompositionLocalProvider(LocalContentColor provides c.ink, content = content)
    }
}

enum class KODevTagTone { Neutral, Accent, Success, Danger }

/** A small square label in capitals for statuses and categories. Not clickable. */
@Composable
fun KODevTag(text: String, modifier: Modifier = Modifier, tone: KODevTagTone = KODevTagTone.Neutral) {
    val c = KODevTheme.colors
    val (frame, fill, ink) = when (tone) {
        KODevTagTone.Neutral -> Triple(c.borderStrong, Color.Transparent, c.ink)
        KODevTagTone.Accent -> Triple(c.accent, c.accent, c.onAccent)
        KODevTagTone.Success -> Triple(c.success, Color.Transparent, c.success)
        KODevTagTone.Danger -> Triple(c.danger, Color.Transparent, c.danger)
    }
    Text(
        text.uppercase(),
        style = KODevType.caption.copy(fontWeight = FontWeight.Bold),
        color = ink,
        modifier = modifier.background(fill).border(1.dp, frame).padding(horizontal = KODevSpace.space2 - 1.dp, vertical = 1.dp),
    )
}
