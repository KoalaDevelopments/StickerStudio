package com.koaladevelopments.kodevui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Raw brand colors. Prefer [KODevColors] in UI. */
object KODevPalette {
    val background = Color(0xFF0E0721)
    val primary = Color(0xFF3B1FDD)
    val surface = Color(0xFF8851FA)
    val secondary = Color(0xFFD697FF)
    val neutral = Color(0xFFE8E8E8)
    val white = Color(0xFFFFFFFF)
}

/** Semantic color tokens. Read them with `KODevTheme.colors`. */
@Immutable
data class KODevColors(
    /** Page and full-screen background. */
    val bg: Color,
    /** Cards, panels and fields on [bg]. */
    val bgRaised: Color,
    /** Main text and icons. */
    val ink: Color,
    /** Secondary text, captions and metadata. */
    val inkMuted: Color,
    /** Titles and highlighted words: lilac on dark, primary on light. */
    val inkAccent: Color,
    /** Fill of the main action. */
    val accent: Color,
    val accentHover: Color,
    val accentPressed: Color,
    /** Text and icons on the accent colors. */
    val onAccent: Color,
    /** Dividers and decorative outlines. Not for controls. */
    val border: Color,
    /** Borders of fields, checkboxes and the secondary button. */
    val borderStrong: Color,
    val focusRing: Color,
    /** Errors and destructive actions. Always with a word or an icon. */
    val danger: Color,
    /** Success and confirmation. Always with a word or an icon. */
    val success: Color,
) {
    /** Links in text. Alias of [inkAccent]. */
    val link: Color get() = inkAccent

    companion object {
        /** The main theme. */
        val Dark = KODevColors(
            bg = KODevPalette.background,
            bgRaised = Color(0xFF1A0E37),
            ink = KODevPalette.white,
            inkMuted = Color(0xFFABA8B1),
            inkAccent = KODevPalette.secondary,
            accent = KODevPalette.primary,
            accentHover = KODevPalette.surface,
            accentPressed = Color(0xFF3019AE),
            onAccent = KODevPalette.white,
            border = Color(0xFF352F45),
            borderStrong = Color(0xFF7A7785),
            focusRing = KODevPalette.secondary,
            danger = Color(0xFFFF4245),
            success = Color(0xFF4DD8E0),
        )

        val Light = Dark.copy(
            bg = KODevPalette.white,
            bgRaised = KODevPalette.neutral,
            ink = KODevPalette.background,
            inkMuted = Color(0xFF625E6F),
            inkAccent = KODevPalette.primary,
            border = Color(0xFFD8D7DB),
            focusRing = KODevPalette.primary,
            danger = Color(0xFFC4001A),
            success = Color(0xFF00727D),
        )
    }
}

internal val LocalKODevColors = staticCompositionLocalOf { KODevColors.Dark }
