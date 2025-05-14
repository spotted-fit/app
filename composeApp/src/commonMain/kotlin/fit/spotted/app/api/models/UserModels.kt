package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

// User search result model
@Serializable
data class UserInfo(
    val id: Int,
    val username: String,
    val avatar: String? = null
)

typealias GetMeResponse = ApiResponse<UserInfo>
typealias UserSearchResults = ApiResponse<List<UserInfo>>

// Profile response model
@Serializable
data class ProfileResponse(
    val id: Int,
    val username: String,
    val avatar: String?,
    val friendsCount: Int,
    val posts: List<ProfilePost>
)

typealias Profile = ApiResponse<ProfileResponse>

// Avatar update response
@Serializable
data class AvatarResponse(
    val avatarUrl: String
)

typealias UpdateAvatarResponse = ApiResponse<AvatarResponse>
