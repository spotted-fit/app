package fit.spotted.app.api

/**
 * WASM/JS implementation of PlatformConfig
 */
actual object PlatformConfig {
    /**
     * Get the base URL for API requests
     * For web browsers, we can use a relative URL or the current domain
     */
    actual fun getBaseUrl(): String = "http://localhost:8080"
}