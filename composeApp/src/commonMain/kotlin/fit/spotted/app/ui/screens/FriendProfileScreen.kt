package fit.spotted.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.ui.theme.LocalSpacing
import kotlinx.coroutines.launch

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

    private val apiClient = ApiProvider.getApiClient()

    @Composable
    override fun Content() {
        // Get standardized spacing values
        val spacing = LocalSpacing.current

        // Haptic feedback for interactions
        val haptic = LocalHapticFeedback.current

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Track whether the screen is fully loaded for animations
        var isReady by remember { mutableStateOf(false) }

        // State for poke button
        var isPoking by remember { mutableStateOf(false) }
        var pokeSuccess by remember { mutableStateOf(false) }

        // Trigger animation after composition
        LaunchedEffect(Unit) {
            isReady = true
        }

        // Reset poke success state after 2 seconds
        LaunchedEffect(pokeSuccess) {
            if (pokeSuccess) {
                kotlinx.coroutines.delay(2000)
                pokeSuccess = false
            }
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

            // Add navigation controls on top with animated entry
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = spacing.statusBarPadding, start = spacing.medium)
                ) {
                    // Back button with improved design
                    Box(
                        modifier = Modifier
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

                    // Add some space between buttons
                    Spacer(modifier = Modifier.width(spacing.medium))

                    // Poke button
                    Box(
                        modifier = Modifier
                            .size(spacing.huge) // Same size as back button
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                when {
                                    pokeSuccess -> MaterialTheme.colors.primary
                                    isPoking -> MaterialTheme.colors.primary.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colors.surface.copy(alpha = 0.9f)
                                }
                            )
                            .clickable(enabled = !isPoking && !pokeSuccess) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                coroutineScope.launch {
                                    isPoking = true
                                    try {
                                        val response = apiClient.pokeUser(
                                            toUsername = username,
                                            title = "Poke from a friend!",
                                            body = "You've been spotted by $username! Go show them what activity you're up to today!"
                                        )

                                        if (response.result == "ok") {
                                            pokeSuccess = true
                                        }
                                    } catch (_: Exception) {
                                    } finally {
                                        isPoking = false
                                    }
                                }
                            }
                            .semantics {
                                contentDescription = "Poke $username"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (pokeSuccess || isPoking) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
} 
