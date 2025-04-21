package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fit.spotted.app.camera.getCamera
import fit.spotted.app.ui.camera.CameraControls
import fit.spotted.app.ui.camera.CameraPreview
import fit.spotted.app.ui.camera.CameraViewModel
import fit.spotted.app.ui.camera.EmojiPicker
import fit.spotted.app.ui.camera.PhotoPreview
import fit.spotted.app.ui.camera.PostAnimation
import fit.spotted.app.ui.camera.PreviewControls
import fit.spotted.app.ui.camera.TimerDisplay
import fit.spotted.app.utils.getImageConverter

/**
 * Screen that allows users to take photos of their fitness activities.
 */
class CameraScreen(
    private val isVisible: Boolean = true,
    private val onAfterWorkoutModeChanged: ((Boolean) -> Unit)? = null
) : Screen {

    @Composable
    override fun Content() {
        // Create the view model
        val viewModel = remember { CameraViewModel() }

        // Notify when isAfterWorkoutMode changes
        LaunchedEffect(viewModel.isAfterWorkoutMode) {
            onAfterWorkoutModeChanged?.invoke(viewModel.isAfterWorkoutMode)
        }

        // Get the camera and image converter
        val camera = remember(isVisible) { 
            if (isVisible) getCamera() else null 
        }

        val imageConverter = remember { getImageConverter() }

        // Update timer every second when running
        LaunchedEffect(viewModel.isTimerRunning) {
            if (viewModel.isTimerRunning) {
                while (true) {
                    kotlinx.coroutines.delay(1000)
                    viewModel.seconds++
                }
            }
        }

        // Handle animation completion and reset states
        LaunchedEffect(viewModel.showPostAnimation) {
            if (viewModel.showPostAnimation) {
                // Wait for animation to complete (1.5 seconds)
                kotlinx.coroutines.delay(1500)

                // Mark animation as finished
                viewModel.completePostAnimation()

                // Wait a bit more for exit animation
                kotlinx.coroutines.delay(500)

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
                    // Show before/after photos in a card deck/carousel style
                    PhotoPreview(
                        beforeWorkoutPhoto = viewModel.beforeWorkoutPhoto!!,
                        afterWorkoutPhoto = viewModel.afterWorkoutPhoto!!,
                        imageConverter = imageConverter,
                        showPostAnimation = viewModel.showPostAnimation,
                        currentPhotoIndex = viewModel.currentPhotoIndex,
                        onNextPhoto = { viewModel.nextPhoto() },
                        onPreviousPhoto = { viewModel.previousPhoto() }
                    )
                } else {
                    // Show camera preview or captured photo
                    CameraPreview(
                        camera = camera,
                        imageConverter = imageConverter,
                        photoData = viewModel.photoData,
                        isVisible = isVisible,
                        photoTaken = viewModel.photoTaken,
                        onPhotoCaptured = { bytes ->
                            viewModel.photoData = bytes
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
                    // Show preview controls
                    PreviewControls(
                        selectedActivity = viewModel.selectedActivity,
                        onActivityClick = { viewModel.showEmojiPicker = true },
                        onRetake = { viewModel.resetToStart() },
                        onPost = { viewModel.postWorkout() },
                        showPostAnimation = viewModel.showPostAnimation
                    )
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
