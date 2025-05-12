package fit.spotted.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classifications for adaptive layouts.
 * These are based on Material Design 3 responsive layout grid breakpoints.
 */
enum class WindowSizeClass {
    COMPACT,    // Phone - typical portrait mode
    MEDIUM,     // Phone in landscape, smaller tablets
    EXPANDED    // Larger tablets and desktop
}

/**
 * Width-based window size class determination
 */
fun getWindowWidthClass(width: Dp): WindowSizeClass = when {
    width < 600.dp -> WindowSizeClass.COMPACT
    width < 840.dp -> WindowSizeClass.MEDIUM
    else -> WindowSizeClass.EXPANDED
}

/**
 * Height-based window size class determination
 */
fun getWindowHeightClass(height: Dp): WindowSizeClass = when {
    height < 480.dp -> WindowSizeClass.COMPACT
    height < 900.dp -> WindowSizeClass.MEDIUM
    else -> WindowSizeClass.EXPANDED
}

/**
 * Represents the size class of a window, consisting of both width and height classes.
 */
data class WindowSize(
    val widthSizeClass: WindowSizeClass,
    val heightSizeClass: WindowSizeClass
)

/**
 * CompositionLocal to provide window size to composables.
 */
val LocalWindowSize = compositionLocalOf { 
    WindowSize(WindowSizeClass.COMPACT, WindowSizeClass.COMPACT) 
} 