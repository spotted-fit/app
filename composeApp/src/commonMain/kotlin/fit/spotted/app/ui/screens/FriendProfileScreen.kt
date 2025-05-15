package fit.spotted.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.ui.theme.LocalSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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
     * and adds ability to poke when clicking on avatar
     */
    private inner class PokableProfileScreen(username: String) : ProfileScreen(username, allowPostDeletion = false) {
        @Composable
        override fun ProfileAvatarWrapper(content: @Composable () -> Unit) {
            Box(contentAlignment = Alignment.Center) {
                // Wrapper for the avatar with a subtle glow effect when poked
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // Add subtle glow animation when poked
                            alpha = 1f // Always visible
                            scaleX = if (showPopup) 1.05f else 1f
                            scaleY = if (showPopup) 1.05f else 1f
                        }
                ) {
                    content()
                }
                
                // Success indicator that appears briefly
                if (showPopup) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                color = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
                
                // Always keep avatar clickable
                val haptic = LocalHapticFeedback.current
                val coroutineScope = rememberCoroutineScope()
                
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            enabled = !isAvatarPoked, // Only check avatar state
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // No visual indication
                        ) {
                            triggerAvatarPoke(haptic, coroutineScope)
                        }
                )
            }
        }
        
        @Composable
        override fun ProfileUsernameWrapper(content: @Composable () -> Unit) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Original username content
                content()
                
                // Visual indicator instead of text - subtle poke success animation
                AnimatedVisibility(
                    visible = showPopup,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(8.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                when {
                                    pokeCount >= 5 -> Color(0xFFFF5722) // Orange for combo
                                    pokeCount >= 3 -> Color(0xFFFFEB3B) // Yellow for streak
                                    else -> MaterialTheme.colors.primary // Default for single poke
                                }
                            )
                    )
                }
            }
        }
    }

    private val apiClient = ApiProvider.getApiClient()
    
    // State for poke button
    private var isPoking by mutableStateOf(false)
    private var pokeSuccess by mutableStateOf(false)
    private var pokeCount by mutableStateOf(0)
    private var lastPokeTime by mutableStateOf(0L)
    private var showPopup by mutableStateOf(false)
    
    // Separate state for avatar and bell to prevent interference
    private var isAvatarPoked by mutableStateOf(false)
    
    // Animation states
    private val buttonScale = Animatable(1f)
    private val rotationAngle = Animatable(0f)
    
    /**
     * Helper function to trigger the poke action (not a composable)
     */
    private fun triggerPoke(
        haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
        coroutineScope: CoroutineScope
    ) {
        // More intense haptic feedback for engagement
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        // Calculate consecutive pokes for streaks
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - lastPokeTime < 10000L) { // 10 second window for streaks
            pokeCount++
        } else {
            pokeCount = 1
        }
        lastPokeTime = currentTime

        coroutineScope.launch {
            isPoking = true
            
            // Animate button press - faster animation
            buttonScale.animateTo(
                targetValue = 0.9f, 
                animationSpec = tween(80, easing = EaseInOutQuad)
            )
            
            try {
                val response = apiClient.pokeUser(
                    toUsername = username,
                )

                if (response.result == "ok") {
                    // Spring animation on success - faster animation
                    buttonScale.animateTo(
                        targetValue = 1.15f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    pokeSuccess = true
                    
                    // Run success animations - faster animation
                    rotationAngle.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(400, easing = EaseOutBack)
                    )
                    
                    // Show popup feedback with shorter display time
                    showPopup = true
                    delay(800) // Shorter delay for a more responsive feel
                    showPopup = false
                    
                    // Reset states after animation - slightly faster
                    rotationAngle.snapTo(0f)
                    delay(300)
                    pokeSuccess = false
                }
            } catch (_: Exception) {
                // Shake animation on failure - faster and less dramatic
                repeat(2) { // Reduced repeats
                    rotationAngle.animateTo(
                        targetValue = 8f, // Less dramatic shake
                        animationSpec = tween(80) // Faster
                    )
                    rotationAngle.animateTo(
                        targetValue = -8f,
                        animationSpec = tween(80)
                    )
                }
                rotationAngle.animateTo(0f, animationSpec = tween(50))
            } finally {
                // Reset scale - quicker
                buttonScale.animateTo(1f, animationSpec = tween(100))
                isPoking = false
            }
        }
    }

    /**
     * Helper function specifically for avatar pokes, separate from bell pokes
     */
    private fun triggerAvatarPoke(
        haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
        coroutineScope: CoroutineScope
    ) {
        // Prevent clicking if already in progress
        if (isAvatarPoked) return
        
        // Haptic feedback
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        // Calculate consecutive pokes for streaks - shared with bell button
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - lastPokeTime < 10000L) { // 10 second window for streaks
            pokeCount++
        } else {
            pokeCount = 1
        }
        lastPokeTime = currentTime

        coroutineScope.launch {
            isAvatarPoked = true
            
            try {
                // Call API without changing bell button state
                val response = apiClient.pokeUser(
                    toUsername = username,
                )

                if (response.result == "ok") {
                    // Show popup feedback with shorter display time
                    showPopup = true
                    delay(500) // Even shorter delay for more responsiveness
                    showPopup = false
                }
            } catch (_: Exception) {
                // Just ignore errors on avatar pokes
            } finally {
                // Very short delay before allowing another click
                delay(100)
                isAvatarPoked = false
            }
        }
    }

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
        
        // Pulsating animation for the idle state
        val infinitePulse = rememberInfiniteTransition()
        val pulseScale by infinitePulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            )
        )

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
                // and with ability to poke on avatar click
                PokableProfileScreen(username = username).Content()
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
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(10f) // Ensure controls are above other content
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
                }
            }
            
            // Fixed position poke button with improved design and size
            AnimatedVisibility(
                visible = isReady,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 300
                    )
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = spacing.statusBarPadding, end = spacing.medium)
                    .zIndex(10f) // Ensure button is above other content
            ) {
                // Improved Poke button with animations and feedback
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // The main poke button - now bigger and fixed position
                    Box(
                        modifier = Modifier
                            .size(spacing.huge) // Larger size button
                            .shadow(
                                elevation = if (isPoking) 2.dp else 8.dp, 
                                shape = CircleShape,
                                spotColor = if (pokeSuccess) MaterialTheme.colors.primary else Color.Gray
                            )
                            .clip(CircleShape)
                            .background(
                                when {
                                    pokeSuccess -> MaterialTheme.colors.primary
                                    isPoking -> MaterialTheme.colors.primary.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colors.surface.copy(alpha = 0.9f)
                                }
                            )
                            .scale(if (!isPoking && !pokeSuccess) pulseScale else 1f)
                            .graphicsLayer(
                                rotationZ = rotationAngle.value,
                                scaleX = buttonScale.value,
                                scaleY = buttonScale.value
                            )
                            .clickable(enabled = !isPoking && !pokeSuccess) {
                                triggerPoke(haptic, coroutineScope)
                            }
                            .semantics {
                                contentDescription = "Poke $username"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (pokeSuccess) Icons.Default.Check else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (pokeSuccess || isPoking) MaterialTheme.colors.onPrimary else MaterialTheme.colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Poke counter badge if there are consecutive pokes
                    if (pokeCount > 1) {
                        Box(
                            modifier = Modifier
                                .size(24.dp) // Slightly larger badge
                                .offset(x = 14.dp, y = (-12).dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5722)), // Orange accent color
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pokeCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp, // Slightly larger text
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
} 
