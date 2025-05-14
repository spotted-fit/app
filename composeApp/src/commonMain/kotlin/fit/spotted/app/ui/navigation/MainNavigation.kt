package fit.spotted.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.ui.screens.*
import kotlinx.coroutines.launch

/**
 * Main navigation component that handles navigation between screens.
 */
@Composable
fun MainNavigation() {
    var isLoggedIn by remember { mutableStateOf(false) }

    val apiClient = ApiProvider.getApiClient()
    val coroutineScope = rememberCoroutineScope()

    if (isLoggedIn) {
        MainScreenWithBottomNav(
            onLogout = {
                coroutineScope.launch {
                    isLoggedIn = false
                    apiClient.logOut()
                }
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                onLogin = {
                    coroutineScope.launch {
                        isLoggedIn = true
                    }
                }
            ).Content()
        }
    }
}

/**
 * Main screen with bottom navigation
 *
 * @param onLogout Callback to be invoked when the user logs out
 */
@Composable
fun MainScreenWithBottomNav(onLogout: () -> Unit) {
    // Use remember to keep the state across recompositions
    var currentTab by remember { mutableStateOf(0) }

    // State for confirmation dialog
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var targetTab by remember { mutableStateOf(0) }

    // Track whether a picture has been taken
    var hasTakenPicture by remember { mutableStateOf(false) }

    // State for friend profile navigation
    var currentFriendProfile by remember { mutableStateOf<String?>(null) }
    var showingFriendProfile by remember { mutableStateOf(false) }

    // Create a pager state with 4 pages (for the 4 tabs)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

    // Function to handle tab changes with confirmation if needed
    fun handleTabChange(newTab: Int) {
        if (currentTab == 1 && newTab != 1 && hasTakenPicture) {
            showConfirmationDialog = true
            targetTab = newTab
        } else {
            // Otherwise, just change the tab
            currentTab = newTab
            // Reset friend profile when changing tabs
            if (newTab != 2) {
                showingFriendProfile = false
                currentFriendProfile = null
            }
        }
    }
    
    // Shared function to navigate to a friend's profile
    fun navigateToFriendProfile(username: String, navigateToFriendsTab: Boolean = false) {
        currentFriendProfile = username
        showingFriendProfile = true
        if (navigateToFriendsTab) {
            handleTabChange(2) // Navigate to the friends tab
        }
    }

    // Sync the pager state with the currentTab
    LaunchedEffect(currentTab) {
        pagerState.scrollToPage(currentTab)
    }

    // Update currentTab when the page changes
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentTab) {
            handleTabChange(pagerState.currentPage)
        }
    }

    // Confirmation dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("Do you really want to exit? Your progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        currentTab = targetTab
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Yes, leave")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Stay")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                elevation = 0.dp,
                contentColor = MaterialTheme.colors.onSurface
            ) {
                // Feed tab
                BottomNavigationItem(
                    selected = currentTab == 0,
                    onClick = {
                        handleTabChange(0)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Feed",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onSurface,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                )

                // Activity/Camera tab
                BottomNavigationItem(
                    selected = currentTab == 1,
                    onClick = {
                        handleTabChange(1)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Activity",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onSurface,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                )

                // Friends tab
                BottomNavigationItem(
                    selected = currentTab == 2,
                    onClick = {
                        handleTabChange(2)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Friends",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onSurface,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                )

                // Profile tab
                BottomNavigationItem(
                    selected = currentTab == 3,
                    onClick = {
                        handleTabChange(3)
                    },
                    icon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.onSurface,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                )
            }
        },
        topBar = {
            if (currentTab == 3) {
                TopAppBar(
                    title = {
                        Text(
                            "Profile",
                            color = MaterialTheme.colors.onSurface
                        )
                    },
                    backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                    elevation = 0.dp,
                    actions = {
                        TextButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colors.onSurface
                            )
                        ) {
                            Text(
                                "Logout",
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            // Disable user scrolling only when on camera tab after taking a picture
            userScrollEnabled = !(currentTab == 1 && hasTakenPicture),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> {
                    val feedScreen = FeedScreen()
                    // Set the navigation callback for the feed screen
                    feedScreen.onNavigateToFriendProfile = { username ->
                        navigateToFriendProfile(username, true)
                    }
                    feedScreen.Content()
                }
                1 -> CameraScreen(
                    isVisible = currentTab == 1,
                    onAfterWorkoutModeChanged = { isAfterWorkoutMode ->
                        hasTakenPicture = isAfterWorkoutMode
                    }
                ).Content()

                2 -> {
                    if (showingFriendProfile && currentFriendProfile != null) {
                        // Show friend profile if a friend is selected
                        FriendProfileScreen(
                            username = currentFriendProfile!!,
                            onNavigateBack = {
                                showingFriendProfile = false
                                currentFriendProfile = null
                            }
                        ).Content()
                    } else {
                        // Show friends list
                        val friendsScreen = FriendsScreen()
                        // Set the navigation callback
                        friendsScreen.onNavigateToFriendProfile = { username ->
                            navigateToFriendProfile(username)
                        }
                        friendsScreen.Content()
                    }
                }

                3 -> ProfileScreen().Content()
                else -> FeedScreen().Content()
            }
        }
    }
}
