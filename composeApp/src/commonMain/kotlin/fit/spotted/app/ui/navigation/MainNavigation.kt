package fit.spotted.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import fit.spotted.app.ui.screens.*

/**
 * Main navigation component that handles navigation between screens.
 */
@Composable
fun MainNavigation() {
    // Check if user is logged in
    var isLoggedIn by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        // Show main screen with bottom navigation when logged in
        MainScreenWithBottomNav(
            onLogout = { isLoggedIn = false }
        )
    } else {
        // Show login screen when not logged in
        Box(modifier = Modifier.fillMaxSize()) {
            // Pass the login callback to the LoginScreen
            LoginScreen(
                onLogin = { isLoggedIn = true }
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

    // Create a pager state with 4 pages (for the 4 tabs)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

    // Function to handle tab changes with confirmation if needed
    fun handleTabChange(newTab: Int) {
        if (currentTab == 1 && newTab != 1 && hasTakenPicture) {
            // If we're leaving the camera tab after taking a picture, show confirmation dialog
            showConfirmationDialog = true
            targetTab = newTab
        } else {
            // Otherwise, just change the tab
            currentTab = newTab
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
            BottomNavigation {
                BottomNavigationItem(
                    selected = currentTab == 0,
                    onClick = { 
                        handleTabChange(0)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )

                BottomNavigationItem(
                    selected = currentTab == 1,
                    onClick = { 
                        handleTabChange(1)
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Activity") },
                    label = { Text("Activity") }
                )

                BottomNavigationItem(
                    selected = currentTab == 2,
                    onClick = { 
                        handleTabChange(2)
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
                    label = { Text("Friends") }
                )

                BottomNavigationItem(
                    selected = currentTab == 3,
                    onClick = { 
                        handleTabChange(3)
                    },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        topBar = {
            if (currentTab == 3) {
                // Show logout button in the top bar when on the profile screen
                TopAppBar(
                    title = { Text("Profile") },
                    actions = {
                        TextButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colors.onPrimary
                            )
                        ) {
                            Text("Logout")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        // Use HorizontalPager instead of Box for swipe navigation
        HorizontalPager(
            state = pagerState,
            // Disable user scrolling only when on camera tab after taking a picture
            userScrollEnabled = !(currentTab == 1 && hasTakenPicture),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            // Render the selected screen's content based on the page
            when (page) {
                0 -> FeedScreen().Content()
                1 -> CameraScreen(
                    isVisible = currentTab == 1,
                    onAfterWorkoutModeChanged = { isAfterWorkoutMode ->
                        hasTakenPicture = isAfterWorkoutMode
                    }
                ).Content()
                2 -> FriendsScreen().Content()
                3 -> ProfileScreen().Content()
                else -> FeedScreen().Content() // Default to Feed screen
            }
        }
    }
}
