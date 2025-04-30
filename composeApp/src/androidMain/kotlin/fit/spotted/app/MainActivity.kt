package fit.spotted.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    // Store permission state
    private var permissionsGranted by mutableStateOf(false)

    // Define the permissions you need
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CAMERA,
    )

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.entries.all { it.value }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if permissions are already granted
        permissionsGranted = checkPermissions()

        // If permissions are not granted, request them immediately
        if (!permissionsGranted) {
            requestPermissions()
        }

        setContent {
            App()
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(requiredPermissions)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}