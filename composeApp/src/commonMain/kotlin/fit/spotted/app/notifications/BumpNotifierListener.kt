package fit.spotted.app.notifications

import com.mmk.kmpnotifier.notification.NotifierManager
import com.russhwolf.settings.Settings

class BumpNotifierListener : NotifierManager.Listener {
    private val settings = Settings()

    override fun onNewToken(token: String) {
        println("onNewToken: $token")
        storeToken(token)
    }

    override fun onPushNotification(title: String?, body: String?) {
        println("Push Notification notification title: $title")
    }

    private fun storeToken(token: String) {
        settings.putString(TOKEN_KEY, token)
    }

    fun getStoredToken(): String? {
        return if (settings.hasKey(TOKEN_KEY)) settings.getString(TOKEN_KEY, "") else null
    }

    fun deleteStoredToken() {
        if (settings.hasKey(TOKEN_KEY)) {
            settings.remove(TOKEN_KEY)
        }
    }

    companion object {
        private const val TOKEN_KEY = "fcm_token"
    }
}
