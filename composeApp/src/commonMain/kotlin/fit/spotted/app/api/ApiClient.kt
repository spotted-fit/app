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
    suspend fun login(password: String, username: String): AuthResponse

    // Posts
    suspend fun createPost(photo1: ByteArray, photo2: ByteArray, emoji: String? = null, text: String? = null): OkResponse
    suspend fun getPost(id: Int): GetPostResponse
    suspend fun likePost(id: Int): OkResponse
    suspend fun unlikePost(id: Int): OkResponse
    suspend fun addComment(postId: Int, text: String): OkResponse
    suspend fun getComments(postId: Int): CommentsList

    // Profile
    suspend fun getUserProfile(id: Int): Profile

    // Search
    suspend fun searchUsers(query: String): UserSearchResults

    // Friends
    suspend fun sendFriendRequest(toId: Int): OkResponse
    suspend fun respondToFriendRequest(requestId: Int, accepted: Boolean): FriendResponseResult
    suspend fun getFriendRequests(): FriendRequests
    suspend fun getFriends(): FriendsList

    suspend fun getFeed(): Feed
}

/**
 * Implementation of the Spotted API client using Ktor
 */
internal class ApiClientImpl : ApiClient {
    private val baseUrl = PlatformConfig.getBaseUrl()
    private var authToken: String? = null

    /**
     * Adds authorization header to the request if an auth token is available.
     * Also sets the accept content type to JSON.
     */
    private fun HttpRequestBuilder.addAuth() {
        accept(ContentType.Application.Json)
        authToken?.let {
            header(HttpHeaders.Authorization, "Bearer $it")
        }
    }

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
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }

    fun setAuthToken(token: String) {
        authToken = token
    }

    /**
     * Registers a new user with the provided credentials.
     * Sets the auth token if registration is successful.
     */
    override suspend fun register(email: String, password: String, username: String): AuthResponse {
        val response = client.post("$baseUrl/register") {
            setBody(RegisterRequest(email, password, username))
            addAuth()
        }
        val authResponse = response.body<AuthResponse>()
        authResponse.response?.token?.let { setAuthToken(it) }
        return authResponse
    }

    /**
     * Logs in a user with the provided credentials.
     * Sets the auth token if login is successful.
     */
    override suspend fun login(password: String, username: String): AuthResponse {
        val response = client.post("$baseUrl/login") {
            setBody(LoginRequest(password, username))
            addAuth()
        }
        val authResponse = response.body<AuthResponse>()
        authResponse.response?.token?.let { setAuthToken(it) }
        return authResponse
    }

    /**
     * Creates a new post with before and after workout photos.
     * 
     * @param photo1 The before workout photo as a byte array
     * @param photo2 The after workout photo as a byte array
     * @param emoji Optional emoji representing the workout type
     * @param text Optional text description of the workout
     * @return OkResponse if successful
     */
    override suspend fun createPost(
        photo1: ByteArray,
        photo2: ByteArray,
        emoji: String?,
        text: String?
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
                text?.let { append("text", it) }
            }
        ) {
            method = HttpMethod.Post
            accept(ContentType.Application.Json)
            authToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
        return response.body()
    }

    /**
     * Gets a post by its ID.
     * 
     * @param id The ID of the post to retrieve
     * @return The post details
     */
    override suspend fun getPost(id: Int): GetPostResponse {
        return client.get("$baseUrl/posts/$id") {
            addAuth()
        }.body()
    }

    /**
     * Likes a post.
     * 
     * @param id The ID of the post to like
     * @return OkResponse if successful
     */
    override suspend fun likePost(id: Int): OkResponse {
        return client.post("$baseUrl/posts/$id/like") {
            addAuth()
        }.body()
    }

    /**
     * Unlikes a post.
     * 
     * @param id The ID of the post to unlike
     * @return OkResponse if successful
     */
    override suspend fun unlikePost(id: Int): OkResponse {
        return client.delete("$baseUrl/posts/$id/like") {
            addAuth()
        }.body()
    }

    /**
     * Adds a comment to a post.
     * 
     * @param postId The ID of the post to comment on
     * @param text The comment text
     * @return OkResponse if successful
     */
    override suspend fun addComment(postId: Int, text: String): OkResponse {
        return client.post("$baseUrl/posts/$postId/comment") {
            setBody(CommentRequest(text))
            addAuth()
        }.body()
    }

    /**
     * Gets all comments for a post.
     * 
     * @param postId The ID of the post to get comments for
     * @return List of comments
     */
    override suspend fun getComments(postId: Int): CommentsList {
        return client.get("$baseUrl/posts/$postId/comments") {
            addAuth()
        }.body()
    }

    /**
     * Gets a user's profile by their ID.
     * 
     * @param id The ID of the user
     * @return The user's profile
     */
    override suspend fun getUserProfile(id: Int): Profile {
        return client.get("$baseUrl/profile/$id") {
            addAuth()
        }.body()
    }

    /**
     * Searches for users by query string.
     * 
     * @param query The search query
     * @return List of matching users
     */
    override suspend fun searchUsers(query: String): UserSearchResults {
        return client.get("$baseUrl/search") {
            parameter("q", query)
            addAuth()
        }.body()
    }

    /**
     * Sends a friend request to another user.
     * 
     * @param toId The ID of the user to send the request to
     * @return OkResponse if successful
     */
    override suspend fun sendFriendRequest(toId: Int): OkResponse {
        return client.post("$baseUrl/friends/request") {
            setBody(FriendRequestRequest(toId))
            addAuth()
        }.body()
    }

    /**
     * Responds to a friend request.
     * 
     * @param requestId The ID of the friend request
     * @param accepted Whether the request was accepted
     * @return Result of the response
     */
    override suspend fun respondToFriendRequest(requestId: Int, accepted: Boolean): FriendResponseResult {
        return client.post("$baseUrl/friends/respond") {
            setBody(FriendResponseRequest(requestId, accepted))
            addAuth()
        }.body()
    }

    /**
     * Gets all pending friend requests for the current user.
     * 
     * @return List of friend requests
     */
    override suspend fun getFriendRequests(): FriendRequests {
        return client.get("$baseUrl/friends/requests") {
            addAuth()
        }.body()
    }

    /**
     * Gets all friends of the current user.
     * 
     * @return List of friends
     */
    override suspend fun getFriends(): FriendsList {
        return client.get("$baseUrl/friends") {
            addAuth()
        }.body()
    }

    override suspend fun getFeed(): Feed {
        return client.get("$baseUrl/feed") {
            addAuth()
        }.body()
    }
}
