package fit.spotted.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive spacing system that scales based on screen size.
 * This extends the basic Spacing class to provide responsive spacing values.
 */
class AdaptiveSpacing(
    private val windowSize: WindowSize,
    private val baseSpacing: Spacing = Spacing()
) {
    val extraSmall: Dp
        @Composable get() = calculateDp(baseSpacing.extraSmall)
        
    val small: Dp
        @Composable get() = calculateDp(baseSpacing.small)
        
    val medium: Dp
        @Composable get() = calculateDp(baseSpacing.medium)
        
    val large: Dp
        @Composable get() = calculateDp(baseSpacing.large)
        
    val extraLarge: Dp
        @Composable get() = calculateDp(baseSpacing.extraLarge)
        
    val huge: Dp
        @Composable get() = calculateDp(baseSpacing.huge)
        
    // Special spacing that scale differently for UX reasons
    val statusBarPadding: Dp
        @Composable get() = when (windowSize.widthSizeClass) {
            WindowSizeClass.COMPACT -> baseSpacing.statusBarPadding
            WindowSizeClass.MEDIUM -> baseSpacing.statusBarPadding * 1.2f
            WindowSizeClass.EXPANDED -> baseSpacing.statusBarPadding * 1.5f
        }
        
    val friendProfileTopPadding: Dp
        @Composable get() = when (windowSize.widthSizeClass) {
            WindowSizeClass.COMPACT -> baseSpacing.friendProfileTopPadding
            WindowSizeClass.MEDIUM -> 100.dp
            WindowSizeClass.EXPANDED -> 80.dp
        }
        
    // Grid spacing for posts grid
    val gridItemSpacing: Dp
        @Composable get() = when (windowSize.widthSizeClass) {
            WindowSizeClass.COMPACT -> 2.dp
            WindowSizeClass.MEDIUM -> 4.dp
            WindowSizeClass.EXPANDED -> 8.dp
        }
    
    // Adaptive column counts for different screen sizes
    val gridColumns: Int
        get() = when (windowSize.widthSizeClass) {
            WindowSizeClass.COMPACT -> 3
            WindowSizeClass.MEDIUM -> 4
            WindowSizeClass.EXPANDED -> 5
        }
        
    // Helper function to calculate adaptive spacing based on screen size
    @Composable
    private fun calculateDp(baseDp: Dp): Dp {
        val scaleFactor = when (windowSize.widthSizeClass) {
            WindowSizeClass.COMPACT -> 1.0f
            WindowSizeClass.MEDIUM -> 1.25f
            WindowSizeClass.EXPANDED -> 1.5f
        }
        return baseDp * scaleFactor
    }
}

/**
 * CompositionLocal to provide adaptive spacing values to composables.
 */
val LocalAdaptiveSpacing = compositionLocalOf {
    AdaptiveSpacing(WindowSize(WindowSizeClass.COMPACT, WindowSizeClass.COMPACT))
} 