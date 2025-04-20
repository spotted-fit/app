package fit.spotted.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fit.spotted.app.getPlatform

// Light theme colors
private val LightColorPalette = lightColors(
    primary = Color(0xFF1976D2),
    primaryVariant = Color(0xFF1565C0),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Dark theme colors
private val DarkColorPalette = darkColors(
    primary = Color(0xFF90CAF9),
    primaryVariant = Color(0xFF64B5F6),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
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
