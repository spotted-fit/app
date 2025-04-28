package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

// Friend request models
@Serializable
data class FriendRequestRequest(
    val toId: Int
)

@Serializable
data class FriendResponseRequest(
    val requestId: Int,
    val accepted: Boolean
)

@Serializable
data class FriendResponseData(
    val response: String
)

typealias FriendResponseResult = ApiResponse<FriendResponseData>

// Friend request preview model
@Serializable
data class FriendRequestPreview(
    val requestId: Int,
    val fromId: Int,
    val username: String
)

// Friend data models
@Serializable
data class FriendData(
    val id: Int,
    val username: String
)

@Serializable
data class FriendsListData(
    val friends: List<FriendData>
)

@Serializable
data class FriendRequestsData(
    val requests: List<FriendRequestPreview>
)

typealias FriendsList = ApiResponse<FriendsListData>
typealias FriendRequests = ApiResponse<FriendRequestsData>
