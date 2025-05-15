package fit.spotted.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmk.kmpnotifier.notification.NotifierManager
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.notifications.BumpNotifierListener
import fit.spotted.app.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
    var isLoggedIn by remember { mutableStateOf(false) }
    val notifierListener = remember { BumpNotifierListener() }
    val apiClient = ApiProvider.getApiClient()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        NotifierManager.addListener(notifierListener)
    }

    if (isLoggedIn) {
        MainScreenWithBottomNav(
            onLogout = {
                coroutineScope.launch {
                    isLoggedIn = false
                    apiClient.logOut()
                    NotifierManager.getPushNotifier().deleteMyToken()
                }
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                onLogin = {
                    coroutineScope.launch {
                        NotifierManager.getPushNotifier().getToken()
                        isLoggedIn = true
                    }
                }
            ).Content()
        }
    }
}

@Composable
fun MainScreenWithBottomNav(onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(0) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var targetTab by remember { mutableStateOf(0) }

    var hasTakenPicture by remember { mutableStateOf(false) }

    var currentFriendProfile by remember { mutableStateOf<String?>(null) }
    var showingFriendProfile by remember { mutableStateOf(false) }
    
    // Challenge screen state
    var showingChallengeDetails by remember { mutableStateOf(false) }
    var currentChallengeId by remember { mutableStateOf<Int?>(null) }
    var showingChallengeInvites by remember { mutableStateOf(false) }
    var showingCreateChallenge by remember { mutableStateOf(false) }
    var showingAchievements by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 }) // Updated to 5 tabs

    fun handleTabChange(newTab: Int) {
        if (currentTab == 1 && newTab != 1 && hasTakenPicture) {
            showConfirmationDialog = true
            targetTab = newTab
        } else {
            currentTab = newTab
            if (newTab != 2) {
                showingFriendProfile = false
                currentFriendProfile = null
            }
            if (newTab != 4) { // Reset challenge screens if not on challenges tab
                showingChallengeDetails = false
                currentChallengeId = null
                showingChallengeInvites = false
                showingCreateChallenge = false
                showingAchievements = false
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
    
    // Challenge navigation functions
    fun navigateToChallengeDetails(challengeId: Int) {
        currentChallengeId = challengeId
        showingChallengeDetails = true
        showingChallengeInvites = false
        showingCreateChallenge = false
        showingAchievements = false
        handleTabChange(4) // Navigate to challenges tab
    }
    
    fun navigateToChallengeInvites() {
        showingChallengeInvites = true
        showingChallengeDetails = false
        showingCreateChallenge = false
        showingAchievements = false
        handleTabChange(4) // Navigate to challenges tab
    }
    
    fun navigateToCreateChallenge() {
        showingCreateChallenge = true
        showingChallengeInvites = false
        showingChallengeDetails = false
        showingAchievements = false
        handleTabChange(4) // Navigate to challenges tab
    }
    
    fun navigateToAchievements() {
        showingAchievements = true
        showingChallengeInvites = false
        showingChallengeDetails = false
        showingCreateChallenge = false
        handleTabChange(4) // Navigate to challenges tab
    }
    
    fun navigateToChallengesList() {
        showingChallengeDetails = false
        currentChallengeId = null
        showingChallengeInvites = false
        showingCreateChallenge = false
        showingAchievements = false
    }

    LaunchedEffect(currentTab) {
        pagerState.scrollToPage(currentTab)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentTab) {
            handleTabChange(pagerState.currentPage)
        }
    }
    
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
                
                // Challenges tab
                BottomNavigationItem(
                    selected = currentTab == 4,
                    onClick = {
                        handleTabChange(4)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Challenges",
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
                    modifier = Modifier
                        .padding(WindowInsets.statusBars.asPaddingValues()),
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
                
                4 -> {
                    // Challenges tab with nested navigation
                    when {
                        showingChallengeDetails && currentChallengeId != null -> {
                            val detailsScreen = ChallengeDetailsScreen(currentChallengeId!!)
                            detailsScreen.onNavigateBack = { navigateToChallengesList() }
                            detailsScreen.onNavigateToUserProfile = { username ->
                                navigateToFriendProfile(username, true)
                            }
                            detailsScreen.Content()
                        }
                        showingChallengeInvites -> {
                            val invitesScreen = ChallengeInvitesScreen()
                            invitesScreen.onNavigateBack = { navigateToChallengesList() }
                            invitesScreen.onNavigateToChallengeDetails = { challengeId ->
                                navigateToChallengeDetails(challengeId)
                            }
                            invitesScreen.Content()
                        }
                        showingCreateChallenge -> {
                            val createScreen = CreateChallengeScreen()
                            createScreen.onNavigateBack = { navigateToChallengesList() }
                            createScreen.onChallengeCreated = { challengeId ->
                                navigateToChallengeDetails(challengeId)
                            }
                            createScreen.Content()
                        }
                        showingAchievements -> {
                            val achievementsScreen = AchievementsScreen()
                            achievementsScreen.onNavigateBack = { navigateToChallengesList() }
                            achievementsScreen.onNavigateToChallenge = { challengeId ->
                                navigateToChallengeDetails(challengeId)
                            }
                            achievementsScreen.Content()
                        }
                        else -> {
                            val challengesScreen = ChallengesScreen()
                            challengesScreen.onNavigateToCreateChallenge = { navigateToCreateChallenge() }
                            challengesScreen.onNavigateToChallengeInvites = { navigateToChallengeInvites() }
                            challengesScreen.onNavigateToChallengeDetails = { challengeId ->
                                navigateToChallengeDetails(challengeId)
                            }
                            challengesScreen.onNavigateToAchievements = { navigateToAchievements() }
                            challengesScreen.Content()
                        }
                    }
                }
                
                else -> FeedScreen().Content()
            }
        }
    }
}
