package fit.spotted.app.api

/**
 * Android implementation of PlatformConfig
 */
actual object PlatformConfig {
    /**
     * Get the base URL for API requests
     * For Android emulators, 10.0.2.2 is the special IP that maps to the host machine's localhost
     */
    actual fun getBaseUrl(): String = "http://10.0.2.2:8080"
}