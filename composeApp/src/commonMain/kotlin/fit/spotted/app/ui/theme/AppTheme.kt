package fit.spotted.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
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

// Composition local for reduced motion setting
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * App theme that wraps MaterialTheme with our custom colors and spacing
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // Get theme preferences
    val themeMode by ThemePreferences.themeMode.collectAsState()
    val reducedMotion by ThemePreferences.reducedMotion.collectAsState()
    
    // Determine if dark theme should be used
    val systemIsDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemIsDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    
    // Choose the appropriate color palette based on theme
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    
    // Provide spacing values and reduced motion setting to all composables in the hierarchy
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalReducedMotion provides reducedMotion
    ) {
        // Use AnimatedVisibility to animate changes between themes
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            MaterialTheme(
                colors = colors,
                content = content
            )
        }
    }
}

/**
 * Helper function to check if reduced motion is enabled
 */
@Composable
fun isReducedMotionEnabled(): Boolean {
    return LocalReducedMotion.current
}
