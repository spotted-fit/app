package fit.spotted.app.api

import fit.spotted.app.BuildKonfig

/**
 * WASM/JS implementation of PlatformConfig
 */
actual object PlatformConfig {
    /**
     * Get the base URL for API requests
     * Using BuildKonfig to get the base URL from build configuration
     * For WASM/JS, we use a relative URL to avoid CORS issues in development
     */
    actual fun getBaseUrl(): String = BuildKonfig.BASE_URL ?: "localhost:8080"
}
