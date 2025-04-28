package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

// User profile model
@Serializable
data class UserProfileData(
    val id: Int,
    val username: String? = null,
    val avatar: String? = null,
    val friendsCount: Int,
    val posts: List<PostData> = emptyList()
)

typealias UserProfile = ApiResponse<UserProfileData>

// User search result model
@Serializable
data class UserSearchResultData(
    val id: Int,
    val username: String,
    val avatar: String? = null
)

typealias UserSearchResults = ApiResponse<List<UserSearchResultData>>
