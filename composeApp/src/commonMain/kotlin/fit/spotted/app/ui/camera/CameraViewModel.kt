package fit.spotted.app.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import fit.spotted.app.api.ApiClient
import fit.spotted.app.emoji.ActivityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * ViewModel for the camera screen that manages the workout photo capture process,
 * including before/after photos, timer, and post creation.
 */
class CameraViewModel(private val apiClient: ApiClient) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    val activityTypes = ActivityType.entries.toList()

    // UI state
    var selectedActivity by mutableStateOf(activityTypes.first())
    var photoTaken by mutableStateOf(false)
    var showEmojiPicker by mutableStateOf(false)
    var photoData by mutableStateOf<ImageBitmap?>(null)
    var isAfterWorkoutMode by mutableStateOf(false)
    var showPreview by mutableStateOf(false)
    var currentPhotoIndex by mutableStateOf(0) // 0 for before, 1 for after

    // Photo data
    var currentPhotoBytes: ByteArray? = null
    private var beforeWorkoutPhotoBytes: ByteArray? = null
    private var afterWorkoutPhotoBytes: ByteArray? = null
    var beforeWorkoutPhoto by mutableStateOf<ImageBitmap?>(null)
    var afterWorkoutPhoto by mutableStateOf<ImageBitmap?>(null)

    // Timer
    var seconds by mutableStateOf(0)
    var isTimerRunning by mutableStateOf(false)
    private var workoutStartTime: Instant? = null

    // Animation
    var showPostAnimation by mutableStateOf(false)
    var postAnimationFinished by mutableStateOf(false)

    // Publishing state
    var isPublishing by mutableStateOf(false)

    /**
     * Updates the timer based on current time and workout start time
     */
    fun updateTimer() {
        workoutStartTime?.let { startTime ->
            val currentTime = Clock.System.now()
            val elapsedSeconds = (currentTime - startTime).inWholeSeconds.toInt()
            seconds = elapsedSeconds
        }
    }

    /**
     * Formats the timer as MM:SS.
     */
    fun formatTimer(): String {
        updateTimer() // Update timer before formatting
        val minutes = (seconds / 60).toString().padStart(2, '0')
        val remainingSeconds = (seconds % 60).toString().padStart(2, '0')
        return "$minutes:$remainingSeconds"
    }

    /**
     * Handles accepting a photo. Stores the photo data and manages state transitions
     * between before and after workout photos.
     */
    fun acceptPhoto() {
        photoData?.let { photo ->
            if (!isAfterWorkoutMode) {
                // Store the before workout photo
                beforeWorkoutPhoto = photo
                currentPhotoBytes?.let { bytes ->
                    beforeWorkoutPhotoBytes = bytes.copyOf()
                }

                // Reset state for next photo
                photoTaken = false
                photoData = null
                currentPhotoBytes = null
                isAfterWorkoutMode = true

                // Start the timer
                seconds = 0
                isTimerRunning = true
                workoutStartTime = Clock.System.now()
            } else {
                // Store the after workout photo
                afterWorkoutPhoto = photo
                currentPhotoBytes?.let { bytes ->
                    afterWorkoutPhotoBytes = bytes.copyOf()
                }

                // Get final timer value
                updateTimer()

                // Stop the timer
                isTimerRunning = false
                workoutStartTime = null

                // Show preview
                showPreview = true
                photoTaken = false
                photoData = null
                currentPhotoBytes = null
            }
        }
    }

    /**
     * Resets the current photo state to allow retaking a photo.
     */
    fun retakePhoto() {
        photoTaken = false
        photoData = null
    }

    /**
     * Creates a post with the before and after workout photos.
     * Shows an animation on success.
     */
    fun postWorkout() {
        // Check if we have both photos and their byte arrays
        if (beforeWorkoutPhoto == null || afterWorkoutPhoto == null ||
            beforeWorkoutPhotoBytes == null || afterWorkoutPhotoBytes == null
        ) {
            return
        }

        // Prevent multiple publish actions
        if (isPublishing) {
            return
        }

        isPublishing = true

        val emojiName = selectedActivity.name
        val beforeBytes = beforeWorkoutPhotoBytes ?: return
        val afterBytes = afterWorkoutPhotoBytes ?: return

        viewModelScope.launch {
            try {
                apiClient.createPost(
                    photo1 = beforeBytes,
                    photo2 = afterBytes,
                    emoji = emojiName,
                    text = "",
                    timer = seconds
                )

                showPostAnimation = true
            } catch (_: Exception) {
                // Error handling could be improved in a future update
                isPublishing = false
            }
        }
    }

    /**
     * Marks the post animation as finished.
     */
    fun completePostAnimation() {
        postAnimationFinished = true
        isPublishing = false
    }

    /**
     * Resets all state to start over with a new workout.
     */
    fun resetToStart() {
        photoTaken = false
        photoData = null
        currentPhotoBytes = null
        beforeWorkoutPhoto = null
        beforeWorkoutPhotoBytes = null
        afterWorkoutPhoto = null
        afterWorkoutPhotoBytes = null
        isAfterWorkoutMode = false
        showPreview = false
        seconds = 0
        isTimerRunning = false
        workoutStartTime = null
        selectedActivity = activityTypes.first()
        showPostAnimation = false
        postAnimationFinished = false
        isPublishing = false
        currentPhotoIndex = 0
    }
}
