package fit.spotted.app.api

/**
 * Android implementation of PlatformConfig
 */
actual object PlatformConfig {
    private const val LOCAL_HOST_URL = "http://10.0.2.2:8080"
    private var localMode: Boolean? = null
    private var customBaseUrl: String? = null

    /**
     * Check if local mode is enabled
     * If true, use localhost IPs
     * If false, use the baseUrl
     */
    actual fun isLocalMode(): Boolean {
        if (localMode == null) {
            // Try to get from system properties first
            val localProp = System.getProperty("local")
            if (localProp != null) {
                localMode = localProp.toBoolean()
            } else {
                // Try to get from environment variables
                val localEnv = System.getenv("LOCAL")
                localMode = localEnv?.toBoolean() ?: true // Default to true if not set
            }
        }
        return localMode ?: true
    }

    /**
     * Get the custom base URL for API requests
     * This is used when local mode is disabled
     */
    actual fun getCustomBaseUrl(): String? {
        if (customBaseUrl == null) {
            // Try to get from system properties first
            val baseUrlProp = System.getProperty("baseUrl")
            if (baseUrlProp != null) {
                customBaseUrl = baseUrlProp
            } else {
                // Try to get from environment variables
                customBaseUrl = System.getenv("BASE_URL")
            }
        }
        return customBaseUrl
    }

    /**
     * Get the base URL for API requests
     * For Android emulators, 10.0.2.2 is the special IP that maps to the host machine's localhost
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
