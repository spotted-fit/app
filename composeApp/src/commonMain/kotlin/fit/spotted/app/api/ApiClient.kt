package fit.spotted.app.api

import fit.spotted.app.api.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Interface for the Spotted API client
 */
interface ApiClient {
    // Authentication
    suspend fun register(email: String, password: String, username: String): AuthResponse
    suspend fun login(email: String, password: String, username: String): AuthResponse

    // Posts
    suspend fun createPost(photo1: ByteArray, photo2: ByteArray, emoji: String? = null, description: String? = null): OkResponse
    suspend fun getPost(id: Int): Post
    suspend fun likePost(id: Int): OkResponse
    suspend fun unlikePost(id: Int): OkResponse
    suspend fun addComment(postId: Int, text: String): OkResponse
    suspend fun getComments(postId: Int): CommentsList

    // Profile
    suspend fun getUserProfile(id: Int): UserProfile

    // Search
    suspend fun searchUsers(query: String): UserSearchResults

    // Friends
    suspend fun sendFriendRequest(toId: Int): OkResponse
    suspend fun respondToFriendRequest(requestId: Int, accepted: Boolean): FriendResponseResult
    suspend fun getFriendRequests(): FriendRequests
    suspend fun getFriends(): FriendsList
}

/**
 * Implementation of the Spotted API client using Ktor
 */
class ApiClientImpl : ApiClient {
    private val baseUrl = PlatformConfig.getBaseUrl()
    private var authToken: String? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
        }
    }

    fun setAuthToken(token: String) {
        authToken = token
    }

    override suspend fun register(email: String, password: String, username: String): AuthResponse {
        val response = client.post("$baseUrl/register") {
            setBody(RegisterRequest(email, password, username))
        }
        val authResponse = response.body<AuthResponse>()
        authResponse.response?.token?.let { setAuthToken(it) }
        return authResponse
    }

    override suspend fun login(email: String, password: String, username: String): AuthResponse {
        val response = client.post("$baseUrl/login") {
            setBody(LoginRequest(email, password, username))
        }
        val authResponse = response.body<AuthResponse>()
        authResponse.response?.token?.let { setAuthToken(it) }
        return authResponse
    }

    override suspend fun createPost(
        photo1: ByteArray,
        photo2: ByteArray,
        emoji: String?,
        description: String?
    ): OkResponse {
        val response = client.submitFormWithBinaryData(
            url = "$baseUrl/posts",
            formData = formData {
                append("photo1", photo1, Headers.build {
                    append(HttpHeaders.ContentDisposition, "form-data; name=photo1; filename=photo1.jpg")
                    append(HttpHeaders.ContentType, "image/jpeg")
                })
                append("photo2", photo2, Headers.build {
                    append(HttpHeaders.ContentDisposition, "form-data; name=photo2; filename=photo2.jpg")
                    append(HttpHeaders.ContentType, "image/jpeg")
                })
                emoji?.let { append("emoji", it) }
                description?.let { append("description", it) }
            }
        ) {
            method = HttpMethod.Post
        }
        return response.body()
    }

    override suspend fun getPost(id: Int): Post {
        return client.get("$baseUrl/posts/$id").body()
    }

    override suspend fun likePost(id: Int): OkResponse {
        return client.post("$baseUrl/posts/$id/like").body()
    }

    override suspend fun unlikePost(id: Int): OkResponse {
        return client.delete("$baseUrl/posts/$id/like").body()
    }

    override suspend fun addComment(postId: Int, text: String): OkResponse {
        return client.post("$baseUrl/posts/$postId/comment") {
            setBody(CommentRequest(text))
        }.body()
    }

    override suspend fun getComments(postId: Int): CommentsList {
        return client.get("$baseUrl/posts/$postId/comments").body()
    }

    override suspend fun getUserProfile(id: Int): UserProfile {
        return client.get("$baseUrl/profile/$id").body()
    }

    override suspend fun searchUsers(query: String): UserSearchResults {
        return client.get("$baseUrl/search") {
            parameter("q", query)
        }.body()
    }

    override suspend fun sendFriendRequest(toId: Int): OkResponse {
        return client.post("$baseUrl/friends/request") {
            setBody(FriendRequestRequest(toId))
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
            accept(ContentType.Application.Json)
        }.body()
    }

    override suspend fun respondToFriendRequest(requestId: Int, accepted: Boolean): FriendResponseResult {
        return client.post("$baseUrl/friends/respond") {
            setBody(FriendResponseRequest(requestId, accepted))
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
            accept(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getFriendRequests(): FriendRequests {
        return client.get("$baseUrl/friends/requests") {
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
            accept(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getFriends(): FriendsList {
        return client.get("$baseUrl/friends") {
            authToken?.let {
                header("Authorization", "Bearer $it")
            }
            accept(ContentType.Application.Json)
        }.body()
    }
}
