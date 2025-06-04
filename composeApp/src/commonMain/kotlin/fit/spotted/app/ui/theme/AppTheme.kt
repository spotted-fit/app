package fit.spotted.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// Light theme colors with a vibrant accent
private val LightColorPalette = lightColors(
    primary = Color(0xFF6200EE),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC5),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Dark theme colors with the same accent
private val DarkColorPalette = darkColors(
    primary = Color(0xFFBB86FC),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC5),
    background = Color.Black,
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
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

    // Default to COMPACT for most devices
    // These can be overridden by platform-specific code as needed
    val windowSize = WindowSize(
        widthSizeClass = WindowSizeClass.COMPACT,
        heightSizeClass = WindowSizeClass.COMPACT
    )

    // Create adaptive spacing based on window size
    val adaptiveSpacing = AdaptiveSpacing(windowSize)

    // Provide spacing values, window size, adaptive spacing, and reduced motion setting to all composables
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalWindowSize provides windowSize,
        LocalAdaptiveSpacing provides adaptiveSpacing,
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

/**
 * Helper function to get current window size class
 */
@Composable
fun currentWindowSize(): WindowSize {
    return LocalWindowSize.current
}

/**
 * Helper extension function to check if the window is in compact width mode
 */
@Composable
fun WindowSize.isCompactWidth(): Boolean {
    return this.widthSizeClass == WindowSizeClass.COMPACT
}

/**
 * Helper extension function to check if the window is in expanded width mode
 */
@Composable
fun WindowSize.isExpandedWidth(): Boolean {
    return this.widthSizeClass == WindowSizeClass.EXPANDED
}
