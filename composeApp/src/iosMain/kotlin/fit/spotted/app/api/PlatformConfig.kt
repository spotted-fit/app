package fit.spotted.app.api

import platform.Foundation.NSProcessInfo

/**
 * iOS implementation of PlatformConfig
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
            // Try to get from environment variables
            val localEnv = NSProcessInfo.processInfo.environment["LOCAL"]?.toString()
            localMode = localEnv?.toBoolean() ?: true // Default to true if not set
        }
        return localMode ?: true
    }

    /**
     * Get the custom base URL for API requests
     * This is used when local mode is disabled
     */
    actual fun getCustomBaseUrl(): String? {
        if (customBaseUrl == null) {
            // Try to get from environment variables
            customBaseUrl = NSProcessInfo.processInfo.environment["BASE_URL"]?.toString()
        }
        return customBaseUrl
    }

    /**
     * Get the base URL for API requests
     * For iOS simulators, localhost or 127.0.0.1 works to access the host machine
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
