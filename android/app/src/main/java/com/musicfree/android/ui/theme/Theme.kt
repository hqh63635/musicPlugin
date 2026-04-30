package com.musicfree.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Aqua500,
    onPrimary = Mint50,
    primaryContainer = Mint300,
    onPrimaryContainer = Navy900,
    secondary = Navy700,
    onSecondary = Mint50,
    tertiary = Coral400,
    background = Mint50,
    onBackground = Navy900,
    surface = Gray50,
    surfaceVariant = Mint100,
    onSurface = Navy900,
    onSurfaceVariant = Gray700,
    outline = Gray300,
)

private val DarkColors = darkColorScheme(
    primary = Aqua400,
    secondary = Mint300,
    tertiary = Coral400,
    background = Navy900,
    surface = Navy700,
    onBackground = Mint50,
    onSurface = Mint50,
)

@Composable
fun MusicfreeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
