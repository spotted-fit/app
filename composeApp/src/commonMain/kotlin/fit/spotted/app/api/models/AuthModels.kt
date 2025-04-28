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
    val email: String,
    val password: String,
    val username: String
)

// Response models
@Serializable
data class AuthResponseData(
    val token: String
)

typealias AuthResponse = ApiResponse<AuthResponseData>
