package com.koaladevelopments.kodevui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * One option of a group where only one counts: a 28dp pixel circle and its
 * label. The whole row is selectable and at least 48dp tall. Put the group
 * under a label naming the question ("Tipo do pack").
 */
@Composable
fun KODevRadio(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = KODevTheme.colors
    val tint = when {
        !enabled -> c.border
        selected -> c.accent
        else -> c.borderStrong
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(KODevSpace.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp)) {
            KODevPixelArt(RING, tint, Modifier.size(28.dp))
            if (selected) KODevPixelArt(DOT, tint, Modifier.size(28.dp))
        }
        Text(label, style = KODevType.body, color = if (enabled) c.ink else c.inkMuted)
    }
}

private val RING = listOf(
    ".....####.....", "...##....##...", "..#........#..", ".#..........#.", ".#..........#.", "#............#", "#............#",
    "#............#", "#............#", ".#..........#.", ".#..........#.", "..#........#..", "...##....##...", ".....####.....",
)
private val DOT = listOf(
    "..............", "..............", "..............", "..............", ".....####.....", "....######....", "....######....",
    "....######....", "....######....", ".....####.....", "..............", "..............", "..............", "..............",
)
