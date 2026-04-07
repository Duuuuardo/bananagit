package com.bananagit.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BananaYellow = Color(0xFFFFE135)
val BananaYellowLight = Color(0xFFFFF9C4)
val BananaYellowDark = Color(0xFFF9A825)
val BananaBrown = Color(0xFF5D4037)
val BananaGreen = Color(0xFF4CAF50)
val BananaRed = Color(0xFFE53935)
val BananaBlue = Color(0xFF1E88E5)
val BananaOrange = Color(0xFFFF9800)
val BananaPurple = Color(0xFF7B1FA2)

private val SurfaceDark = Color(0xFF1E1E2E)
private val SurfaceDarkVariant = Color(0xFF2D2D3F)
private val OnSurfaceDark = Color(0xFFE0E0E0)

private val DarkColorScheme = darkColorScheme(
    primary = BananaYellow,
    onPrimary = Color.Black,
    primaryContainer = BananaYellowDark,
    onPrimaryContainer = Color.Black,
    secondary = BananaGreen,
    onSecondary = Color.White,
    tertiary = BananaBlue,
    onTertiary = Color.White,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = OnSurfaceDark,
    error = BananaRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BananaYellowDark,
    onPrimary = Color.Black,
    primaryContainer = BananaYellowLight,
    onPrimaryContainer = Color.Black,
    secondary = BananaGreen,
    onSecondary = Color.White,
    tertiary = BananaBlue,
    onTertiary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF49454F),
    error = BananaRed,
    onError = Color.White
)

@Composable
fun BananaGitTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
