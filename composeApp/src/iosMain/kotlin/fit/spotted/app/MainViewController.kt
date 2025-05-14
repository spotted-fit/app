package fit.spotted.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val viewController = ComposeUIViewController { App() }
    
    // Initialize FileKit with the view controller
    initializeFileKit(viewController)
    
    return viewController
}