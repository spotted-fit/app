package fit.spotted.app.ui.screens

import androidx.compose.runtime.Composable

/**
 * Base interface for all screens in the application.
 * Each screen should implement this interface to provide its content.
 */
interface Screen {
    /**
     * Provides the content of the screen.
     */
    @Composable
    fun Content()
}