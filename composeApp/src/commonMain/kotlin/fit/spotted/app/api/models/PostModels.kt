package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

/**
 * Detailed data for a post, including all information needed for display.
 *
 * @property id Unique identifier for the post
 * @property userId ID of the user who created the post
 * @property photo1 URL of the first (before workout) photo
 * @property photo2 URL of the second (after workout) photo, may be null
 * @property text Optional text description of the post
 * @property emoji Optional emoji representing the workout type
 * @property createdAt Timestamp when the post was created (in milliseconds since epoch)
 * @property likes Number of likes the post has received
 * @property isLikedByMe Whether the current user has liked this post
 * @property comments List of comments on this post
 */
@Serializable
data class PostDetailedData(
    val id: Int,
    val userId: Int,
    val username: String,
    val photo1: String,
    val photo2: String?,
    val text: String?,
    val emoji: String?,
    val createdAt: Long,
    val likes: Int,
    val isLikedByMe: Boolean,
    val comments: List<CommentData>
)

/**
 * API response wrapper for a detailed post.
 */
typealias GetPostResponse = ApiResponse<PostDetailedData>

/**
 * Simplified post model used in profile views.
 *
 * @property id Unique identifier for the post
 * @property photo1 URL of the first (before workout) photo
 * @property photo2 URL of the second (after workout) photo, may be null
 * @property emoji Optional emoji representing the workout type
 * @property text Optional text description of the post
 * @property createdAt Timestamp when the post was created (in milliseconds since epoch)
 */
@Serializable
data class ProfilePost(
    val id: Int,
    val photo1: String,
    val photo2: String?,
    val emoji: String?,
    val text: String?,
    val createdAt: Long
)

/**
 * Data model for a comment on a post.
 *
 * @property id Unique identifier for the comment
 * @property userId ID of the user who created the comment
 * @property text Content of the comment
 * @property createdAt Timestamp when the comment was created (in milliseconds since epoch)
 */
@Serializable
data class CommentData(
    val id: Int,
    val userId: Int,
    val username: String,
    val text: String,
    val createdAt: Long
)

/**
 * API response wrapper for a list of comments.
 */
typealias CommentsList = ApiResponse<List<CommentData>>

/**
 * Request model for creating a new comment.
 *
 * @property text Content of the comment to be created
 */
@Serializable
data class CommentRequest(
    val text: String
)

typealias Feed = ApiResponse<List<PostDetailedData>>
