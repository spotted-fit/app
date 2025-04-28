package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.FriendData
import kotlinx.coroutines.launch

/**
 * Screen that allows users to find and manage their friends.
 */
class FriendsScreen : Screen {
    // API client
    private val apiClient = ApiProvider.getApiClient()

    // Mock data for friends (used as fallback)
    private val mockFriends = listOf(
        Friend("1", "John Doe", "Running enthusiast", true),
        Friend("2", "Jane Smith", "Yoga instructor", true),
        Friend("3", "Mike Johnson", "Cycling pro", true),
        Friend("4", "Sarah Williams", "Fitness coach", true)
    )

    // Mock data for friend requests
    private val mockFriendRequests = listOf(
        Friend("5", "Alex Brown", "Basketball player", false),
        Friend("6", "Emily Davis", "Swimmer", false)
    )

    // Mock data for search results
    private val mockSearchResults = listOf(
        Friend("7", "David Wilson", "Tennis player", false),
        Friend("8", "Olivia Moore", "Dancer", false),
        Friend("9", "James Taylor", "Hiker", false),
        Friend("10", "Sophia Anderson", "Pilates instructor", false)
    )

    @Composable
    override fun Content() {
        var searchQuery by remember { mutableStateOf("") }
        var isSearching by remember { mutableStateOf(false) }
        var currentTab by remember { mutableStateOf(0) }

        // State for friends, friend requests, search results, loading, and error handling
        var friends by remember { mutableStateOf<List<Friend>?>(null) }
        var friendRequests by remember { mutableStateOf<List<Friend>?>(null) }
        var searchResults by remember { mutableStateOf<List<Friend>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        // Fetch friends and friend requests when the screen is first displayed
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    // Fetch friends
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

                    // Fetch friend requests
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

                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = e.message ?: "An error occurred"
                    isLoading = false
                }
            }
        }

        // Handle search
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotEmpty()) {
                coroutineScope.launch {
                    try {
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
                    } catch (e: Exception) {
                        // Just log the error, don't show it to the user for search
                        println("Search error: ${e.message}")
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    isSearching = it.isNotEmpty()
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                label = { Text("Search for friends") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )

            // Tabs
            TabRow(selectedTabIndex = currentTab) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = { Text("Friends") }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = { Text("Requests") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
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
                return@Column
            }

            // Content based on tab and search state
            when {
                isSearching -> SearchResultsList(searchQuery, searchResults ?: mockSearchResults)
                currentTab == 0 -> FriendsList(friends ?: mockFriends)
                currentTab == 1 -> FriendRequestsList(friendRequests ?: mockFriendRequests)
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
    private fun FriendRequestsList(requests: List<Friend>) {
        // State for request handling
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    Divider()
                }
            }
        }
    }

    @Composable
    private fun FriendItem(friend: Friend, showAddButton: Boolean) {
        // State for button loading
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Coroutine scope for API calls
        val coroutineScope = rememberCoroutineScope()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(40.dp)
            )

            // Friend info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = friend.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = friend.bio.ifEmpty { "Spotted user" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )

                // Show error message if any
                errorMessage?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.error
                    )
                }
            }

            // Add button for requests and search results
            if (showAddButton) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Button(
                        onClick = { 
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    // Send friend request
                                    val response = apiClient.sendFriendRequest(friend.id.toInt())
                                    if (response.result != "ok") {
                                        errorMessage = response.message ?: "Failed to send request"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "An error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Friend"
                        )
                    }
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
