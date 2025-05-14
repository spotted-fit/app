package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

@Serializable
data class LoginRequest(
    val password: String,
    val username: String,
    val firebaseToken: String? = null
)

// Response models
@Serializable
data class AuthResponseData(
    val token: String
)

typealias AuthResponse = ApiResponse<AuthResponseData>
