package fit.spotted.app.ui.components.post.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.emoji.ActivityType
import fit.spotted.app.ui.components.post.state.PostDetailState
import fit.spotted.app.ui.components.post.util.AdaptiveSizes
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import fit.spotted.app.ui.theme.WindowSizeClass
import org.kodein.emoji.compose.WithPlatformEmoji

/**
 * Modern component that displays the user info overlay at the bottom of the post.
 * Features a cleaner design with a prominent activity emoji.
 */
@Composable
fun PostUserInfo(
    userName: String,
    postedAt: String,
    activityType: ActivityType,
    adaptiveSizes: AdaptiveSizes,
    windowSizeClass: WindowSizeClass,
    workoutDuration: String,
    state: PostDetailState? = null,
    onActivityTypeClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val adaptiveSpacing = LocalAdaptiveSpacing.current

    // Activity type animation
    val activityTypeScale by animateFloatAsState(
        targetValue = if (state?.wasActivityTypeClicked == true) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        finishedListener = { if (state?.wasActivityTypeClicked == true) state.resetActivityTypeAnimation() }
    )

    val activityTypeRotation by animateFloatAsState(
        targetValue = if (state?.wasActivityTypeClicked == true) 20f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(adaptiveSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left section: User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = { onProfileClick?.invoke() })
            ) {
                // Profile avatar
                Surface(
                    modifier = Modifier
                        .size(adaptiveSizes.avatarSize)
                        .clip(CircleShape),
                    color = MaterialTheme.colors.surface,
                    elevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(adaptiveSizes.avatarSize / 1.6f),
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(adaptiveSpacing.medium))

                // User info
                Column {
                    Text(
                        text = userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = adaptiveSizes.titleTextSize,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = postedAt,
                            fontSize = adaptiveSizes.captionTextSize,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        // Timer display
                        Spacer(modifier = Modifier.width(adaptiveSpacing.medium))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏱️ $workoutDuration",
                                fontSize = adaptiveSizes.captionTextSize,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Right section: Activity emoji
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = activityTypeScale
                        scaleY = activityTypeScale
                        rotationZ = activityTypeRotation
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = { 
                        onActivityTypeClick?.invoke()
                        state?.triggerActivityTypeAnimation()
                    })
                    .padding(adaptiveSpacing.small),
                contentAlignment = Alignment.Center
            ) {
                WithPlatformEmoji(
                    activityType.emoji
                ) { emojiString, inlineContent ->
                    Text(
                        text = emojiString,
                        inlineContent = inlineContent,
                        fontSize = when (windowSizeClass) {
                            WindowSizeClass.COMPACT -> 42.sp
                            WindowSizeClass.MEDIUM -> 48.sp
                            WindowSizeClass.EXPANDED -> 54.sp
                        },
                        color = Color.White
                    )
                }
            }
        }
    }
}
