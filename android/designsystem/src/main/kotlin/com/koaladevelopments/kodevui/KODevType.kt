package com.koaladevelopments.kodevui

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** The type scale in IBM Plex Mono. No colors: text inherits them. */
object KODevType {
    val family = FontFamily(
        Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
        Font(R.font.ibm_plex_mono_bold, FontWeight.Bold),
    )

    private fun style(size: Int, lineHeight: Int, bold: Boolean) = TextStyle(
        fontFamily = family,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    )

    /** Impact titles in heroes and banners. */
    val display = style(80, 60, bold = true)
    /** Page titles. */
    val headingLarge = style(40, 52, bold = true)
    /** Section titles. */
    val heading = style(32, 42, bold = true)
    /** Subtitles and titles of large cards. */
    val headingSmall = style(24, 31, bold = true)
    /** Titles of cards and list items. */
    val title = style(20, 26, bold = true)
    /** Running text and fields. */
    val body = style(16, 22, bold = false)
    /** [body] in bold: buttons and words that stand out. */
    val bold = style(16, 22, bold = true)
    /** Short labels and tags, preferably in capitals. */
    val caption = style(12, 18, bold = false).copy(letterSpacing = 0.24.em)
}
