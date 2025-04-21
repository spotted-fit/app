package fit.spotted.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fit.spotted.app.getPlatform

// Light theme colors - Modern black and white with transparency
private val LightColorPalette = lightColors(
    primary = Color.Black,
    primaryVariant = Color.Black.copy(alpha = 0.8f),
    secondary = Color.White,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Dark theme colors - Modern black and white with transparency
private val DarkColorPalette = darkColors(
    primary = Color.Black,
    primaryVariant = Color.Black.copy(alpha = 0.7f),
    secondary = Color.White,
    background = Color.Black,
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * App theme that wraps MaterialTheme with our custom colors
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = getDefaultDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}

/**
 * Get the default dark theme setting based on the platform
 * For web, we always use light theme as default
 * For other platforms, we use the system setting
 */
@Composable
private fun getDefaultDarkTheme(): Boolean {
    val platform = getPlatform()
    return if (platform.name.startsWith("Web")) {
        false // Default to light theme for web
    } else {
        isSystemInDarkTheme() // Use system setting for other platforms
    }
}
