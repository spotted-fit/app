package fit.spotted.app.api

/**
 * Platform-specific configuration
 */
expect object PlatformConfig {
    /**
     * Get the base URL for API requests
     * This will be implemented differently for each platform
     */
    fun getBaseUrl(): String
}