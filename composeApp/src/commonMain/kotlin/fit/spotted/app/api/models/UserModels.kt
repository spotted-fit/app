package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

// User search result model
@Serializable
data class UserSearchResultData(
    val id: Int,
    val username: String,
    val avatar: String? = null
)

typealias UserSearchResults = ApiResponse<List<UserSearchResultData>>

// Profile response model
@Serializable
data class ProfileResponse(
    val id: Int,
    val avatar: String?,
    val friendsCount: Int,
    val posts: List<ProfilePost>
)

typealias Profile = ApiResponse<ProfileResponse>
