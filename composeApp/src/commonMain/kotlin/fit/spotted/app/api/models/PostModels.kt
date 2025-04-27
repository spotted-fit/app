package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

// Post models
@Serializable
data class PostData(
    val id: Int,
    val userId: Int,
    val photo1: String,
    val photo2: String? = null,
    val text: String? = null,
    val emoji: String? = null,
    val createdAt: Long,
    val likes: Int,
    val isLikedByMe: Boolean,
    val comments: List<CommentData> = emptyList()
)

typealias Post = ApiResponse<PostData>

// Comment models
@Serializable
data class CommentData(
    val id: Int,
    val userId: Int,
    val text: String,
    val createdAt: Long
)

typealias Comment = CommentData
typealias CommentsList = ApiResponse<List<CommentData>>

// Request models
@Serializable
data class CommentRequest(
    val text: String
)
