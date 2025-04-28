package fit.spotted.app.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import org.kodein.emoji.Emoji
import org.kodein.emoji.activities.sport.Basketball
import org.kodein.emoji.activities.sport.BoxingGlove
import org.kodein.emoji.people_body.person_activity.Running
import org.kodein.emoji.people_body.person_sport.Skier
import org.kodein.emoji.people_body.person_sport.Swimming
import org.kodein.emoji.travel_places.transport_ground.Bicycle

/**
 * ViewModel for the camera screen that manages all state.
 */
class CameraViewModel {
    // Activity types - iOS-style emojis with more variety
    val activityTypes = listOf(
        Emoji.Running,
        Emoji.Bicycle,
        Emoji.Swimming,
        Emoji.Skier,
        Emoji.BoxingGlove,
        Emoji.Basketball
    )

    // Camera state
    var selectedActivity by mutableStateOf(activityTypes.first())
    var photoTaken by mutableStateOf(false)
    var showEmojiPicker by mutableStateOf(false)
    var photoData by mutableStateOf<ImageBitmap?>(null)

    // Before/after workout photos
    var beforeWorkoutPhoto by mutableStateOf<ImageBitmap?>(null)
    var afterWorkoutPhoto by mutableStateOf<ImageBitmap?>(null)
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
                println("Before workout photo captured!")
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
