package fit.spotted.app.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ViewModel for the camera screen that manages all state.
 */
class CameraViewModel {
    // Activity types
    val activityTypes = listOf(
        "🏃",
        "🚶",
        "🚴",
        "🏊",
        "🧘"
    )

    // Camera state
    var selectedActivity by mutableStateOf(activityTypes.first())
    var photoTaken by mutableStateOf(false)
    var showEmojiPicker by mutableStateOf(false)
    var photoData by mutableStateOf<ByteArray?>(null)

    // Before/after workout photos
    var beforeWorkoutPhoto by mutableStateOf<ByteArray?>(null)
    var afterWorkoutPhoto by mutableStateOf<ByteArray?>(null)
    var isAfterWorkoutMode by mutableStateOf(false)
    var showPreview by mutableStateOf(false)

    // Timer state
    var seconds by mutableStateOf(0)
    var isTimerRunning by mutableStateOf(false)

    // Animation state
    var showPostAnimation by mutableStateOf(false)
    var postAnimationFinished by mutableStateOf(false)

    // Carousel state
    var currentPhotoIndex by mutableStateOf(0) // 0 for before, 1 for after

    /**
     * Formats the timer as MM:SS.
     */
    fun formatTimer(): String {
        val minutes = (seconds / 60).toString().padStart(2, '0')
        val remainingSeconds = (seconds % 60).toString().padStart(2, '0')
        return "$minutes:$remainingSeconds"
    }

    /**
     * Handles accepting a photo.
     */
    fun acceptPhoto() {
        if (photoData != null) {
            if (!isAfterWorkoutMode) {
                // First photo (before workout)
                println("Before workout photo captured! Size: ${photoData?.size ?: 0} bytes")
                beforeWorkoutPhoto = photoData
                photoTaken = false
                photoData = null
                isAfterWorkoutMode = true

                // Start the timer
                seconds = 0
                isTimerRunning = true
            } else {
                // Store the after workout photo
                afterWorkoutPhoto = photoData

                // Stop the timer
                isTimerRunning = false

                // Show preview
                showPreview = true
                photoTaken = false
                photoData = null
            }
        }
    }

    /**
     * Handles retaking a photo.
     */
    fun retakePhoto() {
        photoTaken = false
        photoData = null
    }

    /**
     * Handles posting a workout.
     */
    fun postWorkout() {
        showPostAnimation = true
    }

    /**
     * Handles completing the post animation.
     */
    fun completePostAnimation() {
        postAnimationFinished = true
    }

    /**
     * Navigates to the next photo in the carousel.
     */
    fun nextPhoto() {
        if (currentPhotoIndex < 1) {
            currentPhotoIndex++
        }
    }

    /**
     * Navigates to the previous photo in the carousel.
     */
    fun previousPhoto() {
        if (currentPhotoIndex > 0) {
            currentPhotoIndex--
        }
    }

    /**
     * Resets the state to start over.
     */
    fun resetToStart() {
        photoTaken = false
        photoData = null
        beforeWorkoutPhoto = null
        afterWorkoutPhoto = null
        isAfterWorkoutMode = false
        showPreview = false
        seconds = 0
        isTimerRunning = false
        selectedActivity = activityTypes.first()
        showPostAnimation = false
        postAnimationFinished = false
        currentPhotoIndex = 0
    }
}
