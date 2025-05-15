package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

/**
 * Request model for poking another user
 */
@Serializable
data class PokeRequest(
    val toUsername: String,
)