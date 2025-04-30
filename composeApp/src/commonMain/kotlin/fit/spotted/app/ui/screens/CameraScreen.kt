package fit.spotted.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiClient
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.camera.getCamera
import fit.spotted.app.ui.camera.CameraControls
import fit.spotted.app.ui.camera.CameraPreview
import fit.spotted.app.ui.camera.CameraViewModel
import fit.spotted.app.ui.camera.EmojiPicker
import fit.spotted.app.ui.camera.PostAnimation
import fit.spotted.app.ui.camera.TimerDisplay
import fit.spotted.app.ui.components.PostDetailView
import kotlinx.coroutines.delay

/**
 * Screen that allows users to take photos of their fitness activities.
 */
class CameraScreen(
    private val isVisible: Boolean = true,
    private val onAfterWorkoutModeChanged: ((Boolean) -> Unit)? = null
) : Screen {

    @Composable
    override fun Content() {
        // Get the singleton API client
        val apiClient = remember { ApiProvider.getApiClient() }

        // Create the view model
        val viewModel = remember { CameraViewModel(apiClient) }

        // Notify when isAfterWorkoutMode changes
        LaunchedEffect(viewModel.isAfterWorkoutMode) {
            onAfterWorkoutModeChanged?.invoke(viewModel.isAfterWorkoutMode)
        }

        // Get the camera
        val camera = remember(isVisible) { 
            if (isVisible) getCamera() else null 
        }

        // Update timer every second when running
        LaunchedEffect(viewModel.isTimerRunning) {
            if (viewModel.isTimerRunning) {
                while (true) {
                    delay(1000)
                    viewModel.seconds++
                }
            }
        }

        // Handle animation completion and reset states
        LaunchedEffect(viewModel.showPostAnimation) {
            if (viewModel.showPostAnimation) {
                // Wait for animation to complete (1.5 seconds)
                delay(1500)

                // Mark animation as finished
                viewModel.completePostAnimation()

                // Wait a bit more for exit animation
                delay(500)

                // Reset all states after posting
                viewModel.resetToStart()
            }
        }

        // Clean up camera resources when the screen is disposed or becomes invisible
        DisposableEffect(isVisible) {
            onDispose {
                camera?.release()
            }
        }

        // Main container that fills the entire screen
        Box(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
        ) {
            // Post success animation overlay
            PostAnimation(
                visible = viewModel.showPostAnimation,
                animationFinished = viewModel.postAnimationFinished
            )

            // Camera preview or captured photo - takes the full screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3/4f)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.showPreview && viewModel.beforeWorkoutPhoto != null && viewModel.afterWorkoutPhoto != null) {
                    // Show before/after photos using PostDetailView with fly-away animation
                    AnimatedVisibility(
                        visible = !viewModel.showPostAnimation,
                        enter = fadeIn(),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                    ) {
                        PostDetailView(
                            beforeWorkoutPhoto = viewModel.beforeWorkoutPhoto,
                            afterWorkoutPhoto = viewModel.afterWorkoutPhoto,
                            workoutDuration = viewModel.formatTimer(),
                            activityType = viewModel.selectedActivity,
                            userName = "You", // Using "You" as the default username for preview
                            initialShowAfterImage = viewModel.currentPhotoIndex == 1,
                            showBeforeAfterToggle = true,
                            onClose = { viewModel.resetToStart() },
                            showLikesAndComments = false,
                            onActivityTypeClick = { viewModel.showEmojiPicker = true },
                            actionButtons = {
                                // Add a post button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = if (viewModel.isPublishing) 0.4f else 0.7f))
                                        .clickable(
                                            enabled = !viewModel.isPublishing,
                                            onClick = { viewModel.postWorkout() }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        )
                    }
                } else {
                    // Show camera preview or captured photo
                    CameraPreview(
                        camera = camera,
                        photoData = viewModel.photoData,
                        isVisible = isVisible,
                        photoTaken = viewModel.photoTaken,
                        onPhotoCaptured = { capturedPhoto ->
                            viewModel.photoData = capturedPhoto.bitmap
                            viewModel.currentPhotoBytes = capturedPhoto.byteArray
                            viewModel.photoTaken = true
                        }
                    )
                }

                // Show timer in after workout mode
                if (viewModel.isAfterWorkoutMode) {
                    TimerDisplay(
                        timerText = viewModel.formatTimer(),
                        showPostAnimation = viewModel.showPostAnimation,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            // Bottom controls overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (viewModel.showPreview && viewModel.beforeWorkoutPhoto != null && viewModel.afterWorkoutPhoto != null) {
                    // Preview controls are now integrated into PostDetailView
                    // No additional controls needed here
                } else {
                    // Show camera controls
                    CameraControls(
                        camera = camera,
                        photoTaken = viewModel.photoTaken,
                        photoData = viewModel.photoData,
                        isAfterWorkoutMode = viewModel.isAfterWorkoutMode,
                        onRetake = { viewModel.retakePhoto() },
                        onAccept = { viewModel.acceptPhoto() }
                    )
                }
            }

            // Emoji picker popup
            if (viewModel.showEmojiPicker) {
                EmojiPicker(
                    activityTypes = viewModel.activityTypes,
                    selectedActivity = viewModel.selectedActivity,
                    onActivitySelected = { viewModel.selectedActivity = it },
                    onDismiss = { viewModel.showEmojiPicker = false }
                )
            }
        }
    }
}
