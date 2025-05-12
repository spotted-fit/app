package fit.spotted.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import fit.spotted.app.ui.theme.LocalSpacing

/**
 * Screen that displays the profile of a friend.
 * It reuses the functionality of ProfileScreen but adds a back button
 * and removes the ability to delete posts.
 */
class FriendProfileScreen(
    private val username: String,
    private val onNavigateBack: () -> Unit
) : Screen {

    /**
     * A read-only version of the ProfileScreen that doesn't allow post deletion
     */
    private class ReadOnlyProfileScreen(username: String) : ProfileScreen(username, allowPostDeletion = false)

    @Composable
    override fun Content() {
        // Get standardized spacing values
        val spacing = LocalSpacing.current
        
        // Haptic feedback for interactions
        val haptic = LocalHapticFeedback.current
        
        // Track whether the screen is fully loaded for animations
        var isReady by remember { mutableStateOf(false) }
        
        // Trigger animation after composition
        LaunchedEffect(Unit) {
            isReady = true
        }
        
        // Use the existing profile screen to display the friend's profile
        Box {
            // Animated entry for the profile content
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ) + slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetX = { it / 4 }
                ),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                // Display the friend's profile using ProfileScreen without deletion capability
                ReadOnlyProfileScreen(username = username).Content()
            }
            
            // Add a back button on top with animated entry
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 300
                    )
                ),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                // Back button with improved design - using fixed padding for safety
                Box(
                    modifier = Modifier
                        .padding(top = spacing.statusBarPadding, start = spacing.medium)
                        .size(spacing.huge) // Larger touch target
                        .shadow(8.dp, CircleShape) // Better shadow for depth
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.9f))
                        .clickable { 
                            // Add haptic feedback for better user experience
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack() 
                        }
                        // Add semantic description for accessibility
                        .semantics {
                            contentDescription = "Back to friends list"
                        },
                    contentAlignment = Alignment.Center // Ensure content is centered
                ) {
                    // Just the icon, properly centered
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null, // null because we set it on the parent
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
} 