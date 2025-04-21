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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import fit.spotted.app.ui.screens.CameraScreen
import fit.spotted.app.ui.screens.FeedScreen
import fit.spotted.app.ui.screens.FriendsScreen
import fit.spotted.app.ui.screens.LoginScreen
import fit.spotted.app.ui.screens.ProfileScreen
import kotlinx.coroutines.launch

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

    // Create a pager state with 4 pages (for the 4 tabs)
    val pagerState = rememberPagerState(initialPage = 0) { 4 }

    // Create a coroutine scope for launching coroutines
    val coroutineScope = rememberCoroutineScope()

    // Sync the pager state with the currentTab
    LaunchedEffect(currentTab) {
        pagerState.animateScrollToPage(currentTab)
    }

    // Update currentTab when the page changes
    LaunchedEffect(pagerState.currentPage) {
        currentTab = pagerState.currentPage
    }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = currentTab == 0,
                    onClick = { 
                        currentTab = 0
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )

                BottomNavigationItem(
                    selected = currentTab == 1,
                    onClick = { 
                        currentTab = 1
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Activity") },
                    label = { Text("Activity") }
                )

                BottomNavigationItem(
                    selected = currentTab == 2,
                    onClick = { 
                        currentTab = 2
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
                    label = { Text("Friends") }
                )

                BottomNavigationItem(
                    selected = currentTab == 3,
                    onClick = { 
                        currentTab = 3
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            // Render the selected screen's content based on the page
            when (page) {
                0 -> FeedScreen().Content()
                1 -> CameraScreen(isVisible = currentTab == 1).Content()
                2 -> FriendsScreen().Content()
                3 -> ProfileScreen().Content()
                else -> FeedScreen().Content() // Default to Feed screen
            }
        }
    }
}
