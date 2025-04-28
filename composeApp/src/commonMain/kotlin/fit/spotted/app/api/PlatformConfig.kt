package fit.spotted.app.api

/**
 * Platform-specific configuration
 */
expect object PlatformConfig {
    /**
     * Check if local mode is enabled
     * If true, use localhost IPs
     * If false, use the baseUrl
     */
    fun isLocalMode(): Boolean

    /**
     * Get the custom base URL for API requests
     * This is used when local mode is disabled
     */
    fun getCustomBaseUrl(): String?

    /**
     * Get the base URL for API requests
     * This will be implemented differently for each platform
     * If local mode is enabled, use localhost IPs
     * Otherwise, use the custom base URL
     */
    fun getBaseUrl(): String
}
