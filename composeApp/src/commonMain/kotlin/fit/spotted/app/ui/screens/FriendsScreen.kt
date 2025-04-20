package fit.spotted.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen that allows users to find and manage their friends.
 */
class FriendsScreen : Screen {
    
    // Mock data for friends
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
            
            // Content based on tab and search state
            when {
                isSearching -> SearchResultsList(searchQuery, mockSearchResults)
                currentTab == 0 -> FriendsList(mockFriends)
                currentTab == 1 -> FriendRequestsList(mockFriendRequests)
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
        Column {
            Text(
                text = "Friend Requests",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn {
                items(requests) { friend ->
                    FriendItem(friend, showAddButton = true)
                    Divider()
                }
            }
        }
    }
    
    @Composable
    private fun FriendItem(friend: Friend, showAddButton: Boolean) {
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
                    text = friend.bio,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Add button for requests and search results
            if (showAddButton) {
                Button(
                    onClick = { /* Handle add friend action */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Friend"
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
        val isFriend: Boolean
    )
}