package com.example.budgetdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = FreshPurple,
    onPrimary = SoftSurface,
    primaryContainer = FreshPurpleContainer,
    onPrimaryContainer = MainText,

    secondary = FreshBlueText,
    onSecondary = SoftSurface,
    secondaryContainer = FreshBlue,
    onSecondaryContainer = MainText,

    tertiary = FreshGreenText,
    onTertiary = SoftSurface,
    tertiaryContainer = FreshGreen,
    onTertiaryContainer = MainText,

    background = MistBackground,
    onBackground = MainText,

    surface = SoftSurface,
    onSurface = MainText,
    surfaceVariant = GentleSurfaceVariant,
    onSurfaceVariant = SubText,

    outline = SoftOutline,
    outlineVariant = GentleSurfaceVariant,

    error = FreshRedText,
    onError = SoftSurface,
    errorContainer = FreshRed,
    onErrorContainer = FreshRedText
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD7CDF5),
    onPrimary = Color(0xFF2A2433),

    secondary = Color(0xFFCFE2F1),
    onSecondary = Color(0xFF23313F),

    tertiary = Color(0xFFCFE9DB),
    onTertiary = Color(0xFF1F3528),

    background = Color(0xFF1F1C24),
    onBackground = Color(0xFFF3EEF7),

    surface = Color(0xFF2A2630),
    onSurface = Color(0xFFF3EEF7),
    surfaceVariant = Color(0xFF393342),
    onSurfaceVariant = Color(0xFFD0C6DA),

    outline = Color(0xFF5B5466),
    outlineVariant = Color(0xFF393342),

    error = Color(0xFFFFB4B4),
    onError = Color(0xFF561D1D),
    errorContainer = Color(0xFF6F2A2A),
    onErrorContainer = Color(0xFFFFDADA)
)

@Composable
fun BudgetDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}