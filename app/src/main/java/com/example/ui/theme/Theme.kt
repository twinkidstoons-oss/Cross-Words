package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WordlyLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueSoft,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = OrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = Color(0xFF8A4500),
    tertiary = PinkAccent,
    onTertiary = Color.White,
    tertiaryContainer = PinkSoft,
    onTertiaryContainer = Color(0xFF8F124A),
    background = AppBackground,
    onBackground = MainText,
    surface = SurfaceWhite,
    onSurface = MainText,
    surfaceVariant = SurfaceSoft,
    onSurfaceVariant = SecondaryText,
    outline = OutlineSubtle,
    error = WarningRed,
    onError = Color.White,
    errorContainer = WarningRedSoft,
    onErrorContainer = Color(0xFF991B1B),
)

@Composable
fun WordlyPuzzleTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WordlyLightColorScheme,
        typography = WordlyTypography,
        content = content,
    )
}
