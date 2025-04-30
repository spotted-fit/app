package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

// Post preview model
@Serializable
data class PostDetailedData(
    val id: Int,
    val userId: Int,
    val photo1: String,
    val photo2: String?,
    val text: String?,
    val emoji: String?,
    val createdAt: Long,
    val likes: Int,
    val isLikedByMe: Boolean,
    val comments: List<CommentData>
)

typealias GetPostResponse = ApiResponse<PostDetailedData>

// Profile Post model
@Serializable
data class ProfilePost(
    val id: Int,
    val photo1: String,
    val photo2: String?,
    val emoji: String?,
    val text: String?,
    val createdAt: Long
)

@Serializable
data class CommentData(
    val id: Int,
    val userId: Int,
    val text: String,
    val createdAt: Long
)

typealias CommentsList = ApiResponse<List<CommentData>>

// Request models
@Serializable
data class CommentRequest(
    val text: String
)
