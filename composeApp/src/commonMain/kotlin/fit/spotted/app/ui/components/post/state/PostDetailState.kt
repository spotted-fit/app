package fit.spotted.app.ui.components.post.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder class for the PostDetailView component.
 * Encapsulates all state variables and provides methods for handling state changes.
 */
class PostDetailState(
    initialShowAfterImage: Boolean = false,
    initialIsLiked: Boolean = false
) {
    var showAfterImage by mutableStateOf(initialShowAfterImage)
        private set

    var isLiked by mutableStateOf(initialIsLiked)
        private set

    var showComments by mutableStateOf(false)
        private set

    var showDeleteConfirmation by mutableStateOf(false)
        private set

    var wasLikeClicked by mutableStateOf(false)
        private set

    var wasActivityTypeClicked by mutableStateOf(false)
        private set

    fun toggleAfterImage() {
        showAfterImage = !showAfterImage
    }

    fun toggleLike() {
        isLiked = !isLiked
        wasLikeClicked = true
    }

    fun resetLikeAnimation() {
        wasLikeClicked = false
    }

    fun triggerActivityTypeAnimation() {
        wasActivityTypeClicked = true
    }

    fun resetActivityTypeAnimation() {
        wasActivityTypeClicked = false
    }

    fun updateIsLiked(isLiked: Boolean) {
        this.isLiked = isLiked
    }

    fun toggleComments() {
        showComments = !showComments
    }

    fun showDeleteConfirmation() {
        showDeleteConfirmation = true
    }

    fun hideDeleteConfirmation() {
        showDeleteConfirmation = false
    }
}
