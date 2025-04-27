package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

/**
 * Generic response wrapper for API responses
 */
@Serializable
data class ApiResponse<T>(
    val result: String, // "ok" or "error"
    val response: T? = null,
    val message: String? = null // Used for error responses
)

/**
 * Empty response object for endpoints that return just a success status
 */
@Serializable
data class EmptyResponse(val dummy: Boolean = true)

/**
 * Alias for common response types
 */
typealias OkResponse = ApiResponse<EmptyResponse>