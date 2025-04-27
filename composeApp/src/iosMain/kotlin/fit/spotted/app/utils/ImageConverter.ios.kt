package fit.spotted.app.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.style.TextAlign
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.posix.memcpy

/**
 * iOS implementation of the ImageConverter interface.
 * 
 * This implementation displays images using UIKit's UIImageView for better compatibility.
 */
class IosImageConverter : ImageConverter {
    @OptIn(ExperimentalForeignApi::class)
    override fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap? {
        // This is a placeholder implementation
        // Converting to ImageBitmap in iOS is complex and requires more setup
        // We'll use UIImageView directly in the ByteArrayImage composable instead
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun ByteArrayImage(
        bytes: ByteArray,
        modifier: Modifier,
        contentDescription: String?
    ) {
        // Create a UIImage from the ByteArray
        val uiImage = remember(bytes) {
            try {
                // Convert ByteArray to NSData
                val data = bytes.usePinned { pinnedBytes ->
                    NSData.dataWithBytes(pinnedBytes.addressOf(0), bytes.size.toULong())
                }

                // Create UIImage from NSData
                UIImage.imageWithData(data)
            } catch (e: Exception) {
                println("Error creating UIImage: ${e.message}")
                null
            }
        }

        if (uiImage != null) {
            // Display the UIImage using UIKitView and UIImageView
            UIKitView(
                modifier = modifier,
                factory = {
                    val container = UIView(frame = CGRectMake(0.0, 0.0, 100.0, 100.0))
                    val imageView = UIImageView(frame = container.bounds)
                    imageView.setImage(uiImage)
                    imageView.setContentMode(UIViewContentMode.UIViewContentModeScaleAspectFit)
                    container.addSubview(imageView)
                    container
                },
                onResize = { view, rect ->
                    view.setFrame(rect)
                    // Resize the image view to match the container
                    if (view.subviews.isNotEmpty()) {
                        (view.subviews[0] as? UIImageView)?.setFrame(view.bounds)
                    }
                }
            )
        } else {
            // Fallback if image creation fails
            Box(
                modifier = modifier.background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Photo captured (${bytes.size / 1024} KB)\nDisplay failed",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Extension function to convert NSData to ByteArray.
 */
@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}

/**
 * Actual implementation of getImageConverter for iOS.
 */
actual fun getImageConverter(): ImageConverter = IosImageConverter()
