package fit.spotted.app.api

import fit.spotted.app.api.models.PostDetailedData
import fit.spotted.app.api.models.ApiResponse

/**
 * Simple in-memory cache for posts and feed data. It stores responses for a
 * short time to avoid unnecessary network calls when the same data is requested
 * repeatedly. The cache is time-based and expires after [ttlMillis].
 */
internal object ApiCache {
    private const val ttlMillis: Long = 60_000 // 1 minute

    private data class CachedPost(val post: PostDetailedData, val timestamp: Long)
    private data class CachedFeed(val feed: List<PostDetailedData>, val timestamp: Long)

    private val posts = mutableMapOf<Int, CachedPost>()
    private var feed: CachedFeed? = null

    private fun isFresh(timestamp: Long) = (System.currentTimeMillis() - timestamp) < ttlMillis

    fun getPost(id: Int): PostDetailedData? =
        posts[id]?.takeIf { isFresh(it.timestamp) }?.post

    fun savePost(post: PostDetailedData) {
        posts[post.id] = CachedPost(post, System.currentTimeMillis())
    }

    fun removePost(id: Int) {
        posts.remove(id)
    }

    fun getFeed(): List<PostDetailedData>? =
        feed?.takeIf { isFresh(it.timestamp) }?.feed

    fun saveFeed(feedList: List<PostDetailedData>) {
        feed = CachedFeed(feedList, System.currentTimeMillis())
        feedList.forEach { savePost(it) }
    }

    fun clear() {
        posts.clear()
        feed = null
    }
}
