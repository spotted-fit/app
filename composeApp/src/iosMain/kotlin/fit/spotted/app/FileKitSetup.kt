package fit.spotted.app

import file.kit.FileKit
import file.kit.FileKitConfig
import platform.UIKit.UIViewController

/**
 * Initializes FileKit for iOS platform.
 * This should be called from the iOS app entry point.
 *
 * @param viewController The main view controller
 */
fun initializeFileKit(viewController: UIViewController) {
    // Configure FileKit for iOS
    FileKit.init(FileKitConfig(viewController))
}

/**
 * Updates the FileKit configuration with the current view controller.
 * This should be called if the view controller changes.
 *
 * @param viewController The current view controller
 */
fun updateFileKitViewController(viewController: UIViewController) {
    FileKit.updateViewController(viewController)
} 