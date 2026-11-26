package com.koaladevelopments.kodevui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Puts the content in the Design System: dark (the main theme) or light by the
 * system setting, IBM Plex Mono, square shapes. Material components underneath
 * get the same colors and type.
 */
@Composable
fun KODevTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) KODevColors.Dark else KODevColors.Light
    val scheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        background = colors.bg,
        onBackground = colors.ink,
        surface = colors.bg,
        onSurface = colors.ink,
        surfaceVariant = colors.bgRaised,
        onSurfaceVariant = colors.inkMuted,
        surfaceContainer = colors.bgRaised,
        surfaceContainerLow = colors.bgRaised,
        surfaceContainerHigh = colors.bgRaised,
        outline = colors.borderStrong,
        outlineVariant = colors.border,
        error = colors.danger,
    )
    val square = RoundedCornerShape(KODevRadius.none)
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography().run {
            copy(
                bodyLarge = KODevType.body, bodyMedium = KODevType.body, bodySmall = KODevType.caption,
                titleLarge = KODevType.title, labelLarge = KODevType.bold, labelSmall = KODevType.caption,
                headlineSmall = KODevType.headingSmall, headlineMedium = KODevType.heading,
                headlineLarge = KODevType.headingLarge, displayLarge = KODevType.display,
            )
        },
        shapes = Shapes(square, square, square, square, square),
    ) {
        CompositionLocalProvider(
            LocalKODevColors provides colors,
            LocalContentColor provides colors.ink,
            LocalTextStyle provides KODevType.body,
            content = content,
        )
    }
}

/** Access to the current Design System values, as in `KODevTheme.colors.bg`. */
object KODevTheme {
    val colors: KODevColors
        @Composable @ReadOnlyComposable get() = LocalKODevColors.current
}
