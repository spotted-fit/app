package fit.spotted.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Android-specific wrapper around AppTheme that detects screen size and provides
 * appropriate WindowSize. Use this instead of calling AppTheme directly on Android.
 */
@Composable
fun AndroidAdaptiveAppTheme(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val windowSize = remember(screenWidthDp, screenHeightDp) {
        WindowSize(
            widthSizeClass = getWindowWidthClass(screenWidthDp),
            heightSizeClass = getWindowHeightClass(screenHeightDp)
        )
    }

    CompositionLocalProvider(
        LocalWindowSize provides windowSize
    ) {
        AppTheme(content)
    }
} 