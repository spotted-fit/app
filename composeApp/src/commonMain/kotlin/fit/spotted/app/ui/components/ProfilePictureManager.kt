package fit.spotted.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.UpdateAvatarResponse
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch

@Composable
fun ProfilePictureManager(
    currentAvatarUrl: String?,
    onAvatarUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val apiClient = ApiProvider.getApiClient()
    
    // States for profile picture upload
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Create file picker launcher
    val pickLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = { platformFile ->
            platformFile?.let { file ->
                // Read the file bytes in a coroutine
                coroutineScope.launch {
                    try {
                        // Read the bytes - we need to implement this separately
                        val bytes = readFileBytes(file)
                        selectedImageBytes = bytes
                        
                        // Convert ByteArray to ImageBitmap using platform-specific code
                        val bitmap = createImageBitmapFromBytes(bytes)
                        selectedImageBitmap = bitmap
                    } catch (e: Exception) {
                        errorMessage = "Failed to read image: ${e.message}"
                    }
                }
            }
        }
    )
    
    // Function to upload the profile picture
    fun uploadProfilePicture() {
        selectedImageBytes?.let { bytes ->
            isUploading = true
            errorMessage = null
            
            coroutineScope.launch {
                try {
                    val response = apiClient.updateProfileAvatar(bytes)
                    if (response.result == "ok" && response.response != null) {
                        // Update UI with the new avatar URL
                        onAvatarUpdated(response.response.avatarUrl)
                        
                        // Clear selection after successful upload
                        selectedImageBytes = null
                        selectedImageBitmap = null
                    } else {
                        errorMessage = response.message ?: "Failed to upload profile picture"
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: "An error occurred during upload"
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display current avatar or preview of selected image
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.surface)
                .clickable { pickLauncher.launch() },
            contentAlignment = Alignment.Center
        ) {
            when {
                // Show selected image preview if available
                selectedImageBitmap != null -> {
                    Image(
                        bitmap = selectedImageBitmap!!,
                        contentDescription = "Profile picture preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Show current avatar if available
                currentAvatarUrl != null -> {
                    SubcomposeAsyncImage(
                        model = currentAvatarUrl,
                        contentDescription = "Current profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 2.dp
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier.size(45.dp),
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    )
                }
                // Show placeholder
                else -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Select profile picture",
                        modifier = Modifier.size(45.dp),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
            
            // Show loading indicator when uploading
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Only show upload button if an image is selected
        if (selectedImageBytes != null && !isUploading) {
            Button(
                onClick = { uploadProfilePicture() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Profile Picture")
            }
        } else if (!isUploading) {
            Button(
                onClick = { pickLauncher.launch() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Profile Picture")
            }
        }
        
        // Show error message if any
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// This function will be implemented differently per platform
expect fun createImageBitmapFromBytes(bytes: ByteArray): ImageBitmap

// This function will be implemented differently per platform
expect suspend fun readFileBytes(file: PlatformFile): ByteArray 