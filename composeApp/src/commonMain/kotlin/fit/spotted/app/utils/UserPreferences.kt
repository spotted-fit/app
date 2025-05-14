package fit.spotted.app.utils

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Handles persistent user preferences across the application
 */
class UserPreferences(private val settings: Settings) {
    
    companion object {
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        
        // Singleton instance
        private var instance: UserPreferences? = null
        
        fun getInstance(): UserPreferences {
            if (instance == null) {
                instance = UserPreferences(Settings())
            }
            return instance!!
        }
    }
    
    /**
     * Gets the saved avatar URL, or null if not set
     */
    fun getAvatarUrl(): String? {
        return settings[KEY_AVATAR_URL]
    }
    
    /**
     * Saves the user's avatar URL
     */
    fun saveAvatarUrl(url: String) {
        settings[KEY_AVATAR_URL] = url
    }
    
    /**
     * Clears the saved avatar URL
     */
    fun clearAvatarUrl() {
        settings.remove(KEY_AVATAR_URL)
    }
    
    /**
     * Gets the user ID, or null if not set
     */
    fun getUserId(): String? {
        return settings[KEY_USER_ID]
    }
    
    /**
     * Saves the user ID
     */
    fun saveUserId(userId: String) {
        settings[KEY_USER_ID] = userId
    }
    
    /**
     * Gets the user name, or null if not set
     */
    fun getUserName(): String? {
        return settings[KEY_USER_NAME]
    }
    
    /**
     * Saves the user name
     */
    fun saveUserName(name: String) {
        settings[KEY_USER_NAME] = name
    }
    
    /**
     * Clears all user data
     */
    fun clearUserData() {
        settings.remove(KEY_AVATAR_URL)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
    }
} 