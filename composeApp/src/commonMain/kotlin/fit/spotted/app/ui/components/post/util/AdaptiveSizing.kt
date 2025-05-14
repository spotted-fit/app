package fit.spotted.app.ui.components.post.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.ui.theme.WindowSizeClass

/**
 * Data class that holds adaptive sizes for different screen sizes.
 */
data class AdaptiveSizes(
    val buttonSize: Dp,
    val iconSize: Dp,
    val titleTextSize: TextUnit,
    val captionTextSize: TextUnit,
    val avatarSize: Dp
)

/**
 * Composable function that returns adaptive sizes based on the window size.
 */
@Composable
fun rememberAdaptiveSizes(windowSizeClass: WindowSizeClass): AdaptiveSizes {
    return remember(windowSizeClass) {
        AdaptiveSizes(
            buttonSize = when (windowSizeClass) {
                WindowSizeClass.COMPACT -> 56.dp
                WindowSizeClass.MEDIUM -> 64.dp
                WindowSizeClass.EXPANDED -> 72.dp
            },
            iconSize = when (windowSizeClass) {
                WindowSizeClass.COMPACT -> 28.dp
                WindowSizeClass.MEDIUM -> 32.dp
                WindowSizeClass.EXPANDED -> 36.dp
            },
            titleTextSize = when (windowSizeClass) {
                WindowSizeClass.COMPACT -> 16.sp
                WindowSizeClass.MEDIUM -> 18.sp
                WindowSizeClass.EXPANDED -> 20.sp
            },
            captionTextSize = when (windowSizeClass) {
                WindowSizeClass.COMPACT -> 14.sp
                WindowSizeClass.MEDIUM -> 16.sp
                WindowSizeClass.EXPANDED -> 18.sp
            },
            avatarSize = when (windowSizeClass) {
                WindowSizeClass.COMPACT -> 40.dp
                WindowSizeClass.MEDIUM -> 48.dp
                WindowSizeClass.EXPANDED -> 56.dp
            }
        )
    }
}