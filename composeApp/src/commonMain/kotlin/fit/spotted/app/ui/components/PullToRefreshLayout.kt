package fit.spotted.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A custom Pull-to-Refresh layout that provides an intuitive and visually pleasing
 * refresh experience for content.
 *
 * @param isRefreshing Whether the content is currently refreshing.
 * @param onRefresh Callback to be invoked when the user pulls to refresh.
 * @param content The content to display.
 */
@Composable
fun PullToRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val refreshDistance = with(LocalDensity.current) { 80.dp.toPx() }
    val refreshTriggerDistance = refreshDistance * 0.75f

    var refreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    val pullProgress = (pullDistance / refreshDistance).coerceIn(0f, 1f)

    // Animated spinner rotation
    val spinnerAngle by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val coroutineScope = rememberCoroutineScope()

    // Set isRefreshing based on the parent's isRefreshing state
    LaunchedEffect(isRefreshing) {
        refreshing = isRefreshing
    }

    // When pull is released and distance was greater than the trigger, start refreshing
    fun onRelease() {
        if (pullDistance > refreshTriggerDistance && !refreshing) {
            refreshing = true
            onRefresh()

            // Reset pull distance with animation
            coroutineScope.launch {
                val startDistance = pullDistance
                val animationSpec = tween<Float>(300, easing = FastOutSlowInEasing)
                val animator = Animatable(startDistance)
                animator.animateTo(
                    targetValue = 0f,
                    animationSpec = animationSpec
                ) {
                    pullDistance = value
                }
            }
        } else if (pullDistance > 0) {
            // Just animate back to 0 if not triggering refresh
            coroutineScope.launch {
                val startDistance = pullDistance
                val animationSpec = tween<Float>(200, easing = FastOutSlowInEasing)
                val animator = Animatable(startDistance)
                animator.animateTo(
                    targetValue = 0f,
                    animationSpec = animationSpec
                ) {
                    pullDistance = value
                }
            }
        }
    }

    // Nested scroll connection to detect pull gesture
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If refreshing or pulling down, consume the scroll
                if (refreshing || available.y < 0 || pullDistance > 0) {
                    if (pullDistance > 0) {
                        pullDistance = (pullDistance + available.y).coerceAtLeast(0f)
                        return available
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Only allow pulling down when at the top of the list
                if (!refreshing && available.y > 0) {
                    pullDistance += available.y
                    return available
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                onRelease()
                return Velocity.Zero
            }
        }
    }

    // Update refreshing state when external isRefreshing changes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && refreshing) {
            // Delay to show the completion animation
            delay(300)
            refreshing = false
        }
    }

    // Layout with nested scroll connection
    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Main content
        content()

        // Pull indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { pullDistance.toDp() })
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            if (pullDistance > 0 || refreshing) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(if (refreshing) spinnerAngle else pullProgress * 360),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = if (refreshing) 1f else pullProgress,
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                if (pullDistance > refreshTriggerDistance && !refreshing) {
                    Text(
                        text = "Release to refresh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(top = 50.dp)
                    )
                } else if (pullDistance > 0 && !refreshing) {
                    Text(
                        text = "Pull to refresh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 50.dp)
                    )
                }
            }
        }
    }
} 