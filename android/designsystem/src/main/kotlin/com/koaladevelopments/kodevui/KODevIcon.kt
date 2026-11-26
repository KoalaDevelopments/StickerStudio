package com.koaladevelopments.kodevui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A pixel icon on the 11 by 11 grid, one-pixel stroke, drawn at 24dp (2dp per
 * pixel) in [tint], the content color by default. [label] is what TalkBack
 * says; null hides it, for an icon next to text.
 */
@Composable
fun KODevIcon(rows: List<String>, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current, label: String? = null) {
    KODevPixelArt(rows, tint, modifier.size(24.dp).then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier))
}

/** Pixel-art rows (a `#` per pixel) drawn at 2dp per pixel, centered in the space it gets. */
@Composable
internal fun KODevPixelArt(rows: List<String>, tint: Color, modifier: Modifier) {
    Canvas(modifier) {
        val px = 2.dp.toPx()
        val columns = rows.maxOf { it.length }
        val left = (size.width - columns * px) / 2
        val top = (size.height - rows.size * px) / 2
        for ((y, row) in rows.withIndex()) {
            for ((x, char) in row.withIndex()) {
                if (char == '#') drawRect(tint, Offset(left + x * px, top + y * px), Size(px, px))
            }
        }
    }
}

/** The interface icons. Same data as kodev_ui's KODevIcons: a `#` per pixel. */
object KODevIcons {
    val arrowLeft = listOf("...........", "...........", "....#......", "...#.......", "..#........", ".#########.", "..#........", "...#.......", "....#......", "...........", "...........")
    val chevronLeft = listOf("...........", "...........", "......#....", ".....#.....", "....#......", "...#.......", "....#......", ".....#.....", "......#....", "...........", "...........")
    val chevronRight = listOf("...........", "...........", "....#......", ".....#.....", "......#....", ".......#...", "......#....", ".....#.....", "....#......", "...........", "...........")
    val chevronDown = listOf("...........", "...........", "...........", "..#.....#..", "...#...#...", "....#.#....", ".....#.....", "...........", "...........", "...........", "...........")
    val menu = listOf("...........", "...........", ".#########.", "...........", "...........", ".#########.", "...........", "...........", ".#########.", "...........", "...........")
    val close = listOf("...........", ".#.......#.", "..#.....#..", "...#...#...", "....#.#....", ".....#.....", "....#.#....", "...#...#...", "..#.....#..", ".#.......#.", "...........")
    val plus = listOf("...........", ".....#.....", ".....#.....", ".....#.....", ".....#.....", ".#########.", ".....#.....", ".....#.....", ".....#.....", ".....#.....", "...........")
    val check = listOf("...........", "...........", "...........", ".........#.", "........#..", ".#.....#...", "..#...#....", "...#.#.....", "....#......", "...........", "...........")
    val search = listOf("...........", "..####.....", ".#....#....", ".#....#....", ".#....#....", ".#....#....", "..####.#...", "........#..", ".........#.", "..........#", "...........")
    val home = listOf("...........", ".....#.....", "....#.#....", "...#...#...", "..#.....#..", ".#.......#.", "..#.....#..", "..#.###.#..", "..#.#.#.#..", "..###.###..", "...........")
    val user = listOf("...........", "....###....", "...#...#...", "...#...#...", "...#...#...", "....###....", "...........", "..#######..", ".#.......#.", ".#########.", "...........")
    val bell = listOf(".....#.....", "....###....", "...#...#...", "..#.....#..", "..#.....#..", "..#.....#..", "..#.....#..", ".#.......#.", ".#########.", "...........", "....###....")
    val heart = listOf("...........", "...........", "..##...##..", ".#..#.#..#.", ".#...#...#.", ".#.......#.", "..#.....#..", "...#...#...", "....#.#....", ".....#.....", "...........")
    val more = listOf("...........", "...........", "...........", "...........", "...........", ".##..##..##", ".##..##..##", "...........", "...........", "...........", "...........")

    val all = listOf(arrowLeft, chevronLeft, chevronRight, chevronDown, menu, close, plus, check, search, home, user, bell, heart, more)
}
