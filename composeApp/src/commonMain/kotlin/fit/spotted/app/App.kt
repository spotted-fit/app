package fit.spotted.app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import fit.spotted.app.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        // Use the main navigation component
        fit.spotted.app.ui.navigation.MainNavigation()
    }
}
