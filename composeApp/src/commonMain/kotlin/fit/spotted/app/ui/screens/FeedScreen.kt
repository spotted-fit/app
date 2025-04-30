package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.GetPostResponse
import fit.spotted.app.ui.components.Comment
import fit.spotted.app.ui.components.PostDetailView
import fit.spotted.app.ui.components.toUiComment
import kotlinx.coroutines.launch
import org.kodein.emoji.Emoji
import org.kodein.emoji.people_body.person_activity.Running
import org.kodein.emoji.people_body.person_sport.Skier
import org.kodein.emoji.people_body.person_sport.Swimming
import org.kodein.emoji.travel_places.transport_ground.Bicycle

/**
 * Screen that displays the feed of photos from friends in a full-screen TikTok-like style.
 */
class FeedScreen : Screen {
    @Composable
    override fun Content() {
        // State for posts, loading, and error handling
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val apiClient by remember { mutableStateOf(ApiProvider.getApiClient()) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Fetch posts when the screen is first displayed
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
//                    val request = apiClient.getF

                    // Use mock data for now
                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = e.message ?: "An error occurred"
                    isLoading = false
                }
            }
        }

        // Show loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        // Show error message
        errorMessage?.let {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return
        }
    }
}
