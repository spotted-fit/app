package fit.spotted.app

import androidx.compose.runtime.Composable
import fit.spotted.app.ui.navigation.MainNavigation
import fit.spotted.app.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        // Use the main navigation component
        MainNavigation()
    }
}
