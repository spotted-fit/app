package fit.spotted.app.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable that displays a timer in the top-right corner.
 */
@Composable
fun TimerDisplay(
    timerText: String,
    showPostAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier
            .padding(16.dp)
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        visible = !showPostAnimation,
        enter = fadeIn(),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = timerText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
