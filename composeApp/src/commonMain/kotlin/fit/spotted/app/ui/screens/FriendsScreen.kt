package fit.spotted.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen that allows users to find and manage their friends.
 */
class FriendsScreen : Screen {
    // API client
    private val apiClient = ApiProvider.getApiClient()

    // Callback for navigating to a friend's profile
    var onNavigateToFriendProfile: ((String) -> Unit)? = null

    @Composable
    override fun Content() {
        var searchQuery by remember { mutableStateOf("") }
        var isSearching by remember { mutableStateOf(false) }
        var currentTab by remember { mutableStateOf(0) }

        // State for friends, friend requests, search results, loading, and error handling
        var friends by remember { mutableStateOf<List<Friend>?>(null) }
        var friendRequests by remember { mutableStateOf<List<Friend>?>(null) }
        var searchResults by remember { mutableStateOf<List<Friend>?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showLoadingIndicator by remember { mutableStateOf(true) }

        // Animation states
        val tabTransition = updateTransition(targetState = currentTab, label = "Tab Transition")
        val contentAlpha by tabTransition.animateFloat(
            transitionSpec = { tween(durationMillis = 300) },
            label = "Content Alpha"
        ) { 1f }

        // Track which screen is fully ready
        var isScreenReady by remember { mutableStateOf(false) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Function to fetch friends
        suspend fun fetchFriends() {
            try {
                showLoadingIndicator = true
                errorMessage = null

                delay(200)

                val friendsResponse = apiClient.getFriends()
                if (friendsResponse.result == "ok") {
                    friends = friendsResponse.response?.friends?.map { friendData ->
                        Friend(
                            id = friendData.id.toString(),
                            name = friendData.username,
                            bio = "", // API doesn't provide bio
                            isFriend = true
                        )
                    } ?: emptyList()
                }
                delay(300)
                showLoadingIndicator = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
                showLoadingIndicator = false
            }
        }

        suspend fun fetchFriendRequests() {
            try {
                showLoadingIndicator = true
                errorMessage = null

                delay(200)

                val requestsResponse = apiClient.getFriendRequests()
                if (requestsResponse.result == "ok") {
                    friendRequests = requestsResponse.response?.requests?.map { requestPreview ->
                        Friend(
                            id = requestPreview.requestId.toString(),
                            name = requestPreview.username,
                            bio = "", // API doesn't provide bio
                            isFriend = false,
                            fromId = requestPreview.fromId
                        )
                    } ?: emptyList()
                }

                delay(300)
                showLoadingIndicator = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "An error occurred"
                showLoadingIndicator = false
            }
        }

        // Fetch initial data and refresh when tab changes
        LaunchedEffect(currentTab) {
            coroutineScope.launch {
                // Fetch data based on current tab
                when (currentTab) {
                    0 -> fetchFriends()
                    1 -> fetchFriendRequests()
                }
            }
        }

        // Mark screen as ready after initial load
        LaunchedEffect(Unit) {
            delay(500)
            isScreenReady = true
        }

        // Handle search
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotEmpty()) {
                coroutineScope.launch {
                    try {
                        // Show loading indicator with a small delay
                        showLoadingIndicator = true
                        delay(300)

                        val response = apiClient.searchUsers(searchQuery)
                        if (response.result == "ok") {
                            searchResults = response.response?.map { userData ->
                                Friend(
                                    id = userData.id.toString(),
                                    name = userData.username,
                                    bio = "", // API doesn't provide bio
                                    isFriend = false
                                )
                            } ?: emptyList()
                        }

                        // Hide loading indicator after search completes
                        showLoadingIndicator = false
                    } catch (e: Exception) {
                        // Just log the error, don't show it to the user for search
                        println("Search error: ${e.message}")
                        showLoadingIndicator = false
                    }
                }
            }
        }

        // Main UI
        AnimatedVisibility(
            visible = isScreenReady,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                    expandVertically(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = LocalAdaptiveSpacing.current.statusBarPadding,
                        bottom = 16.dp
                    )
            ) {
                // Animated title
                Text(
                    text = if (isSearching) "Search Results" else if (currentTab == 0) "Your Friends" else "Friend Requests",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .animateContentSize()
                )

                // Search bar with improved styling
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        isSearching = it.isNotEmpty()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colors.surface),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primary,
                        unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                        backgroundColor = MaterialTheme.colors.surface
                    ),
                    label = { Text("Search for friends") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    singleLine = true
                )

                // Tabs with improved styling
                TabRow(
                    selectedTabIndex = currentTab,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.primary,
                    divider = {
                        Divider(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                            thickness = 2.dp
                        )
                    },
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                            height = 3.dp,
                            color = MaterialTheme.colors.primary
                        )
                    }
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = {
                            if (currentTab != 0) {
                                currentTab = 0
                                searchQuery = "" // Clear search when switching tabs
                                isSearching = false
                            }
                        },
                        text = {
                            Text(
                                "Friends",
                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = {
                            if (currentTab != 1) {
                                currentTab = 1
                                searchQuery = "" // Clear search when switching tabs
                                isSearching = false
                            }
                        },
                        text = {
                            Text(
                                "Requests",
                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated content container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .graphicsLayer { alpha = contentAlpha }
                ) {
                    // Loading indicator
                    if (showLoadingIndicator) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsating animation for loading indicator
                            val infiniteTransition = rememberInfiniteTransition()
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(60.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                color = MaterialTheme.colors.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    // Error message
                    if (errorMessage != null && !showLoadingIndicator) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .shadow(4.dp, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.error.copy(alpha = 0.1f))
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Error",
                                    color = MaterialTheme.colors.error,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colors.error,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            when (currentTab) {
                                                0 -> fetchFriends()
                                                1 -> fetchFriendRequests()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.error
                                    )
                                ) {
                                    Text("Try Again", color = Color.White)
                                }
                            }
                        }
                    }

                    // Content when not loading or error
                    if (!showLoadingIndicator && errorMessage == null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Content based on current state
                            when {
                                isSearching -> SearchResultsList(searchQuery, searchResults ?: emptyList())
                                currentTab == 0 -> FriendsList(friends ?: emptyList())
                                currentTab == 1 -> FriendRequestsList(initialRequests = friendRequests ?: emptyList())
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SearchResultsList(query: String, results: List<Friend>) {
        Column {
            Text(
                text = "Search Results for \"$query\"",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(results) { friend ->
                    FriendItem(friend, showAddButton = true)
                    Divider()
                }
            }
        }
    }

    @Composable
    private fun FriendsList(friends: List<Friend>) {
        Column {
            Text(
                text = "Your Friends",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(friends) { friend ->
                    FriendItem(friend, showAddButton = false)
                    Divider()
                }
            }
        }
    }

    @Composable
    private fun FriendRequestsList(
        initialRequests: List<Friend>,
    ) {
        // State for request handling
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        // Track requests that have been accepted or rejected
        var requests by remember { mutableStateOf(initialRequests) }
        // Track which requests have been accepted (to show "added" text)
        var acceptedRequestIds by remember { mutableStateOf(setOf<String>()) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        Column {
            Text(
                text = "Friend Requests",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Show error message if any
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Show loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn {
                items(requests) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Friend info (using 60% of width)
                        Box(modifier = Modifier.weight(0.6f)) {
                            Column {
                                Text(
                                    text = friend.name,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = friend.bio.ifEmpty { "Wants to be your friend" },
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Accept/Reject buttons (using 40% of width)
                        Row(
                            modifier = Modifier.weight(0.4f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Check if this request has been accepted
                            if (acceptedRequestIds.contains(friend.id)) {
                                // Show "added" text
                                Text(
                                    text = "Added",
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else {
                                // Accept button
                                Button(
                                    onClick = {
                                        isLoading = true
                                        errorMessage = null
                                        coroutineScope.launch {
                                            try {
                                                // Accept friend request
                                                val response = apiClient.respondToFriendRequest(
                                                    requestId = friend.id.toInt(), // id is the requestId for friend requests
                                                    accepted = true
                                                )
                                                if (response.result != "ok") {
                                                    errorMessage = response.message ?: "Failed to accept request"
                                                } else {
                                                    requests = requests.filter { it.id != friend.id }
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = e.message ?: "An error occurred"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(end = 8.dp),
                                    enabled = !isLoading
                                ) {
                                    Text("Accept")
                                }

                                // Reject button
                                OutlinedButton(
                                    onClick = {
                                        isLoading = true
                                        errorMessage = null
                                        coroutineScope.launch {
                                            try {
                                                // Reject friend request
                                                val response = apiClient.respondToFriendRequest(
                                                    requestId = friend.id.toInt(), // id is the requestId for friend requests
                                                    accepted = false
                                                )
                                                if (response.result != "ok") {
                                                    errorMessage = response.message ?: "Failed to reject request"
                                                } else {
                                                    // Remove the request from the list
                                                    requests = requests.filter { it.id != friend.id }
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = e.message ?: "An error occurred"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }

    @Composable
    private fun FriendItem(friend: Friend, showAddButton: Boolean) {
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // Click animation
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = if (friend.isFriend) 4.dp else 2.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = friend.isFriend) {
                    // Navigate to friend profile when clicked
                    if (friend.isFriend) {
                        isPressed = true
                        coroutineScope.launch {
                            delay(100) // Short delay for animation
                            onNavigateToFriendProfile?.invoke(friend.name)
                            delay(200) // Short delay to reset animation
                            isPressed = false
                        }
                    }
                },
            elevation = 0.dp,
            backgroundColor = if (friend.isFriend)
                MaterialTheme.colors.surface
            else
                MaterialTheme.colors.surface.copy(alpha = 0.7f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile pic with improved styling
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Name and info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = friend.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (friend.isFriend) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Friend",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colors.secondary.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (showAddButton) "Suggested" else "Request",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colors.secondary
                                    )
                                }
                            }
                        }
                    }

                    if (showAddButton) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        isLoading = true
                                        val response = apiClient.sendFriendRequest(friend.id.toInt())
                                        isLoading = false
                                        if (response.result != "ok") {
                                            errorMessage = response.message ?: "Failed to send friend request"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "An error occurred"
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .padding(0.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = CircleShape,
                            enabled = !isLoading && errorMessage == null,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Friend",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Show error message if present
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colors.error,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colors.error.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Data class for mock data
    data class Friend(
        val id: String,
        val name: String,
        val bio: String,
        val isFriend: Boolean,
        val fromId: Int? = null // Added for friend requests
    )
}
