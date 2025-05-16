package fit.spotted.app.api

import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import fit.spotted.app.api.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Interface for the Spotted API client
 */
interface ApiClient {
    // Authentication
    suspend fun register(email: String, password: String, username: String, firebaseToken: String?): AuthResponse
    suspend fun login(password: String, username: String, firebaseToken: String?): AuthResponse
    fun isLoggedIn(): Boolean
    fun logOut()
    
    // Auth error handling
    fun setAuthErrorCallback(callback: () -> Unit)
    
    // Token validation
    suspend fun validateToken(): Boolean

    // Posts
    suspend fun createPost(
        photo1: ByteArray,
        photo2: ByteArray,
        emoji: String? = null,
        text: String? = null,
        timer: Int
    ): OkResponse

    suspend fun getPost(id: Int): GetPostResponse
    suspend fun deletePost(id: Int): OkResponse
    suspend fun likePost(id: Int): OkResponse
    suspend fun unlikePost(id: Int): OkResponse
    suspend fun addComment(postId: Int, text: String): OkResponse
    suspend fun getComments(postId: Int): CommentsList

    // Profile
    suspend fun getUserProfile(username: String): Profile
    suspend fun getMe(): GetMeResponse

    // Search
    suspend fun searchUsers(query: String): UserSearchResults

    // Friends
    suspend fun sendFriendRequest(toId: Int): OkResponse
    suspend fun respondToFriendRequest(requestId: Int, accepted: Boolean): FriendResponseResult
    suspend fun getFriendRequests(): FriendRequests
    suspend fun getFriends(): FriendsList

    // Notifications
    suspend fun pokeUser(toUsername: String): OkResponse

    suspend fun getFeed(): Feed
    
    // Challenges
    suspend fun getChallenges(): ChallengesList
    suspend fun getChallenge(id: Int): ChallengeResponse
    suspend fun createChallenge(request: CreateChallengeRequest): ChallengeResponse
    suspend fun getChallengeInvites(): ChallengeInvitesList
    suspend fun respondToChallengeInvite(challengeId: Int, accepted: Boolean): OkResponse
    suspend fun leaveChallenge(challengeId: Int): OkResponse
    
    // Achievements
    suspend fun getAchievements(): AchievementsList
    suspend fun getAchievement(id: Int): Achievement
}

/**
 * Implementation of the Spotted API client using Ktor
 */
internal class ApiClientImpl : ApiClient {
    private val baseUrl = PlatformConfig.getBaseUrl()
    private var authToken: String?
        get() = settings.getStringOrNull("authToken")
        set(value) {
            if (value != null) {
                settings.putString("authToken", value)
            } else {
                settings.remove("authToken")
            }
        }
    private val settings = Settings()
    override fun isLoggedIn() = "authToken" in settings
    override fun logOut() = settings.remove("authToken")
    
    // Auth error callback
    private var onAuthError: (() -> Unit)? = null
    
    override fun setAuthErrorCallback(callback: () -> Unit) {
        onAuthError = callback
    }
    
    // Helper method to handle auth errors consistently
    private fun handle401Error() {
        // Always clear the token first
        logOut()
        
        // Then call the callback on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            onAuthError?.invoke()
        }
    }
    
    /**
     * Validates the current auth token by making a lightweight API call.
     * Returns true if the token is valid, false otherwise.
     */
    override suspend fun validateToken(): Boolean {
        if (!isLoggedIn()) return false
        
        return try {
            // Use the "me" endpoint which should be lightweight and available in most APIs
            val response = client.get("$baseUrl/me") {
                addAuth()
            }
            
            // Check status manually to catch 401s that might not throw exceptions
            if (response.status.value == 401) {
                handle401Error()
                return false
            }
            
            // If we get here, the token is valid (no 401 was thrown)
            response.status.isSuccess()
        } catch (e: Exception) {
            // Check if it's a 401 error
            if (e is ClientRequestException && e.response.status.value == 401) {
                handle401Error()
            }
            // For any exception, consider the token invalid
            false
        }
    }

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
        
        // Handle HTTP errors globally
        install(HttpCallValidator) {
            handleResponseExceptionWithRequest { exception, _ ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                
                // Check if it's a 401 Unauthorized error
                if (clientException.response.status.value == 401) {
                    handle401Error()
                }
                
                // Let the exception propagate so it can be handled by the calling code
                throw exception
            }
            
            // Also check responses directly to catch 401s that might not throw exceptions
            validateResponse { response ->
                if (response.status.value == 401) {
                    handle401Error()
                    throw ClientRequestException(response, "HTTP 401: Unauthorized")
                }
            }
        }
        
        defaultRequest {
            contentType(ContentType.Application.Json)
            authToken?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }

    /**
     * Registers a new user with the provided credentials.
     * Sets the auth token if registration is successful.
     */
    override suspend fun register(email: String, password: String, username: String, firebaseToken: String?): AuthResponse {
        val response = client.post("$baseUrl/register") {
            setBody(RegisterRequest(email, password, username, firebaseToken))
            addAuth()
        }
        val authResponse = response.body<AuthResponse>()
        authToken = authResponse.response?.token
        return authResponse
    }

    /**
     * Logs in a user with the provided credentials.
     * Sets the auth token if login is successful.
     * @param firebaseToken Optional Firebase token for push notifications
     */
    override suspend fun login(password: String, username: String, firebaseToken: String?): AuthResponse {
        val response = client.post("$baseUrl/login") {
            setBody(LoginRequest(password, username, firebaseToken))
            addAuth()
        }
        val authResponse = response.body<AuthResponse>()
        authToken = authResponse.response?.token
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
        text: String?,
        timer: Int
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
                append("timer", timer)
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
     * Deletes a post.
     *
     * @param id The ID of the post to delete
     * @return OkResponse if successful
     */
    override suspend fun deletePost(id: Int): OkResponse {
        return client.delete("$baseUrl/posts/$id") {
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
     * @param username The username of the user
     * @return The user's profile
     */
    override suspend fun getUserProfile(username: String): Profile {
        return client.get("$baseUrl/profile/$username") {
            addAuth()
        }.body()
    }

    /**
     * Retrieves the authenticated user's information.
     *
     * @return UserInfo containing the details of the current user.
     */
    override suspend fun getMe(): GetMeResponse {
        return client.get("$baseUrl/me") {
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

    /**
     * Sends a poke notification to another user.
     *
     * @param toUsername The username of the user to poke
     * @return OkResponse if successful
     */
    override suspend fun pokeUser(toUsername: String): OkResponse {
        return client.post("$baseUrl/friends/poke") {
            setBody(PokeRequest(toUsername))
            addAuth()
        }.body()
    }

    // Challenges API
    
    /**
     * Gets all challenges for the current user.
     *
     * @return List of challenges
     */
    override suspend fun getChallenges(): ChallengesList {
        return client.get("$baseUrl/challenges") {
            addAuth()
        }.body()
    }
    
    /**
     * Gets a challenge by its ID.
     *
     * @param id The ID of the challenge to retrieve
     * @return The challenge details
     */
    override suspend fun getChallenge(id: Int): ChallengeResponse {
        return client.get("$baseUrl/challenges/$id") {
            addAuth()
        }.body()
    }
    
    /**
     * Creates a new challenge.
     *
     * @param request The challenge creation request
     * @return The created challenge
     */
    override suspend fun createChallenge(request: CreateChallengeRequest): ChallengeResponse {
        return client.post("$baseUrl/challenges") {
            setBody(request)
            addAuth()
        }.body()
    }
    
    /**
     * Gets all challenge invites for the current user.
     *
     * @return List of challenge invites
     */
    override suspend fun getChallengeInvites(): ChallengeInvitesList {
        return client.get("$baseUrl/challenges/invites") {
            addAuth()
        }.body()
    }
    
    /**
     * Responds to a challenge invite.
     *
     * @param challengeId The ID of the challenge
     * @param accepted Whether the invite was accepted
     * @return OkResponse if successful
     */
    override suspend fun respondToChallengeInvite(challengeId: Int, accepted: Boolean): OkResponse {
        return client.post("$baseUrl/challenges/invites/respond") {
            setBody(ChallengeInviteResponse(challengeId, accepted))
            addAuth()
        }.body()
    }
    
    /**
     * Leaves a challenge.
     *
     * @param challengeId The ID of the challenge to leave
     * @return OkResponse if successful
     */
    override suspend fun leaveChallenge(challengeId: Int): OkResponse {
        return client.delete("$baseUrl/challenges/$challengeId/leave") {
            addAuth()
        }.body()
    }
    
    /**
     * Gets all achievements for the current user.
     *
     * @return List of achievements
     */
    override suspend fun getAchievements(): AchievementsList {
        return client.get("$baseUrl/achievements") {
            addAuth()
        }.body()
    }
    
    /**
     * Gets an achievement by its ID.
     *
     * @param id The ID of the achievement to retrieve
     * @return The achievement details
     */
    override suspend fun getAchievement(id: Int): Achievement {
        return client.get("$baseUrl/achievements/$id") {
            addAuth()
        }.body()
    }
}
