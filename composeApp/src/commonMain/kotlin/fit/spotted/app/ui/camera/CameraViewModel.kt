package fit.spotted.app.ui.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import fit.spotted.app.api.ApiClient
import fit.spotted.app.camera.CapturedImage
import fit.spotted.app.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
class CameraViewModel(private val apiClient: ApiClient) {
    // Create a CoroutineScope for this ViewModel
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
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

    // Photo byte arrays
    var currentPhotoBytes: ByteArray? = null
    private var beforeWorkoutPhotoBytes: ByteArray? = null
    private var afterWorkoutPhotoBytes: ByteArray? = null

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
                println("Debug - currentPhotoBytes before assignment: ${currentPhotoBytes != null}")

                // Store the before workout photo
                beforeWorkoutPhoto = photoData
                currentPhotoBytes?.let { bytes ->
                    beforeWorkoutPhotoBytes = bytes.copyOf()
                }

                println("Debug - beforeWorkoutPhoto after assignment: ${beforeWorkoutPhoto != null}")
                println("Debug - beforeWorkoutPhotoBytes after assignment: ${beforeWorkoutPhotoBytes != null}")

                // Reset state for next photo
                photoTaken = false
                photoData = null
                currentPhotoBytes = null
                isAfterWorkoutMode = true

                // Start the timer
                seconds = 0
                isTimerRunning = true
            } else {
                // Store the after workout photo
                println("After workout photo captured!")
                println("Debug - currentPhotoBytes before assignment: ${currentPhotoBytes != null}")

                afterWorkoutPhoto = photoData
                currentPhotoBytes?.let { bytes ->
                    afterWorkoutPhotoBytes = bytes.copyOf()
                }

                println("Debug - afterWorkoutPhoto after assignment: ${afterWorkoutPhoto != null}")
                println("Debug - afterWorkoutPhotoBytes after assignment: ${afterWorkoutPhotoBytes != null}")

                // Stop the timer
                isTimerRunning = false

                // Show preview
                showPreview = true
                photoTaken = false
                photoData = null
                currentPhotoBytes = null
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
        // Debug logging to see what's happening
        println("Debug - beforeWorkoutPhoto: ${beforeWorkoutPhoto != null}")
        println("Debug - afterWorkoutPhoto: ${afterWorkoutPhoto != null}")
        println("Debug - beforeWorkoutPhotoBytes: ${beforeWorkoutPhotoBytes != null}")
        println("Debug - afterWorkoutPhotoBytes: ${afterWorkoutPhotoBytes != null}")

        // Check if we have both photos and their byte arrays
        if (beforeWorkoutPhoto == null || afterWorkoutPhoto == null ||
            beforeWorkoutPhotoBytes == null || afterWorkoutPhotoBytes == null) {
            // Handle error case - both photos are required
            println("Debug - Exiting postWorkout early because one of the required properties is null")
            return
        }

        // Get emoji as string
        val emojiString = selectedActivity.toString()

        // Launch coroutine to call API
        viewModelScope.launch {
            try {
                // Call API to create post
                val response = apiClient.createPost(
                    photo1 = beforeWorkoutPhotoBytes!!,
                    photo2 = afterWorkoutPhotoBytes!!,
                    emoji = emojiString,
                    text = "" // No text description for now
                )

                // Show success animation
                showPostAnimation = true
            } catch (e: Exception) {
                // Handle error
                println("Error creating post: ${e.message}")
                // We could show an error message to the user here
            }
        }
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
        currentPhotoBytes = null
        beforeWorkoutPhoto = null
        beforeWorkoutPhotoBytes = null
        afterWorkoutPhoto = null
        afterWorkoutPhotoBytes = null
        isAfterWorkoutMode = false
        showPreview = false
        seconds = 0
        isTimerRunning = false
        selectedActivity = activityTypes.first()
        showPostAnimation = false
        postAnimationFinished = false
        currentPhotoIndex = 0
    }

    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}
