package fit.spotted.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized spacing values to be used throughout the app.
 * This ensures consistent spacing across different components.
 */
@Immutable
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val statusBarPadding: Dp = 60.dp,
    val friendProfileTopPadding: Dp = 150.dp
)

/**
 * CompositionLocal to provide spacing values to composables.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() } 