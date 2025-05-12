package fit.spotted.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enumeration of possible theme modes
 */
enum class ThemeMode {
    SYSTEM, // Follow system setting
    LIGHT,  // Always use light theme
    DARK    // Always use dark theme
}

/**
 * Singleton that manages theme preferences across the app
 */
object ThemePreferences {
    // Default to system theme
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    // Whether animations should be reduced
    private val _reducedMotion = MutableStateFlow(false)
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()
    
    /**
     * Set the theme mode
     * @param mode The desired theme mode
     */
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
    
    /**
     * Toggle between light and dark modes
     * If currently using system theme, will switch to either light or dark based on current system setting
     * @param currentSystemIsDark Whether the system is currently in dark mode
     */
    fun toggleTheme(currentSystemIsDark: Boolean) {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.SYSTEM -> if (currentSystemIsDark) ThemeMode.LIGHT else ThemeMode.DARK
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
        }
    }
    
    /**
     * Set reduced motion preference
     * @param enabled Whether reduced motion should be enabled
     */
    fun setReducedMotion(enabled: Boolean) {
        _reducedMotion.value = enabled
    }
    
    /**
     * Toggle reduced motion setting
     */
    fun toggleReducedMotion() {
        _reducedMotion.value = !_reducedMotion.value
    }
} 