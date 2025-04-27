package fit.spotted.app.api

/**
 * iOS implementation of PlatformConfig
 */
actual object PlatformConfig {
    /**
     * Get the base URL for API requests
     * For iOS simulators, localhost or 127.0.0.1 works to access the host machine
     */
    actual fun getBaseUrl(): String = "http://localhost:8080"
}