package fit.spotted.app.api

/**
 * Provider for the API client
 */
object ApiProvider {
    private val _apiClient: ApiClient by lazy { ApiClientImpl() }

    fun getApiClient(): ApiClient = _apiClient
}
