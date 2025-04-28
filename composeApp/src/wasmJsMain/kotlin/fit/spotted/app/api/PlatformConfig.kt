package fit.spotted.app.api

import kotlinx.browser.window

/**
 * WASM/JS implementation of PlatformConfig
 */
actual object PlatformConfig {
    private const val LOCAL_HOST_URL = "http://localhost:8080"
    private var localMode: Boolean? = null
    private var customBaseUrl: String? = null

    /**
     * Check if local mode is enabled
     * If true, use localhost IPs
     * If false, use the baseUrl
     */
    actual fun isLocalMode(): Boolean {
        if (localMode == null) {
            // Try to get from URL parameters
            val urlParams = window.location.search.substring(1)
                .split("&")
                .associate { 
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }

            val localParam = urlParams["local"]
            localMode = localParam?.toBoolean() ?: true // Default to true if not set
        }
        return localMode ?: true
    }

    /**
     * Get the custom base URL for API requests
     * This is used when local mode is disabled
     */
    actual fun getCustomBaseUrl(): String? {
        if (customBaseUrl == null) {
            // Try to get from URL parameters
            val urlParams = window.location.search.substring(1)
                .split("&")
                .associate { 
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }

            customBaseUrl = urlParams["baseUrl"]
        }
        return customBaseUrl
    }

    /**
     * Get the base URL for API requests
     * For web browsers, we can use a relative URL or the current domain
     * If local mode is enabled, use localhost IPs
     * Otherwise, use the custom base URL
     */
    actual fun getBaseUrl(): String {
        return if (isLocalMode()) {
            LOCAL_HOST_URL
        } else {
            getCustomBaseUrl() ?: LOCAL_HOST_URL
        }
    }
}
