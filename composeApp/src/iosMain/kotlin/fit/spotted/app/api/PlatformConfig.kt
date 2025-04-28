package fit.spotted.app.api

import fit.spotted.app.BuildKonfig

/**
 * iOS implementation of PlatformConfig
 */
actual object PlatformConfig {
    /**
     * Get the base URL for API requests
     * Using BuildKonfig to get the base URL from build configuration
     */
    actual fun getBaseUrl(): String = BuildKonfig.BASE_URL ?: "http://10.0.2.2:8080"
}
