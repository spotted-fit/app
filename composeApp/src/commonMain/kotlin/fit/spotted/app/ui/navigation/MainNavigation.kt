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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mmk.kmpnotifier.notification.NotifierManager
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.notifications.BumpNotifierListener
import fit.spotted.app.ui.screens.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainNavigation() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(true) }
    val notifierListener = remember { BumpNotifierListener() }
    val apiClient = ApiProvider.getApiClient()
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    
    // State to track if user was logged out due to auth error
    var showAuthErrorMessage by remember { mutableStateOf(false) }
    
    // Function to handle logout (used by both explicit logout and auth errors)
    fun performLogout() {
        coroutineScope.launch {
            // Run on Main dispatcher to ensure immediate UI updates
            withContext(Dispatchers.Main) {
                apiClient.logOut()
                NotifierManager.getPushNotifier().deleteMyToken()
                isLoggedIn = false
            }
        }
    }
    
    // Function to validate the token
    fun validateToken() {
        coroutineScope.launch {
            // Only validate if currently logged in
            if (isLoggedIn) {
                val isValid = apiClient.validateToken()
                if (!isValid) {
                    // If token is invalid, log out
                    performLogout()
                    showAuthErrorMessage = true
                }
            }
        }
    }

    // Observe lifecycle to validate token when app comes to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // App came to foreground, validate token
                validateToken()
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        NotifierManager.addListener(notifierListener)
        
        // Register callback for auth errors (like 401)
        apiClient.setAuthErrorCallback {
            // This callback runs on the Main dispatcher from ApiClient
            performLogout()
            showAuthErrorMessage = true
        }
        
        // Validate token on startup instead of just checking if it exists
        isLoggedIn = if (apiClient.isLoggedIn()) {
            // If token exists, validate it
            apiClient.validateToken()
        } else {
            false
        }
        
        // Initialization complete
        isInitializing = false
    }
    
    // Show error message if needed
    LaunchedEffect(showAuthErrorMessage) {
        if (showAuthErrorMessage) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = "Your session has expired. Please log in again.",
                duration = SnackbarDuration.Short
            )
            showAuthErrorMessage = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    backgroundColor = Color.Red.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    snackbarData = data
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading indicator during initial auth check
            if (isInitializing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Only show content after initialization is complete
            else if (isLoggedIn) {
                MainScreenWithBottomNav(
                    onLogout = {
                        performLogout() // Use the shared logout function
                    }
                )
            } else {
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
        if (currentTab == 2 && newTab != 2 && hasTakenPicture) {
            showConfirmationDialog = true
            targetTab = newTab
        } else {
            currentTab = newTab
            if (newTab != 1) {
                showingFriendProfile = false
                currentFriendProfile = null
            }
            if (newTab != 3) {
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
            handleTabChange(1)
        }
    }
    
    // Challenge navigation functions
    fun navigateToChallengeDetails(challengeId: Int) {
        currentChallengeId = challengeId
        showingChallengeDetails = true
        showingChallengeInvites = false
        showingCreateChallenge = false
        showingAchievements = false
        handleTabChange(3)
    }
    
    fun navigateToChallengeInvites() {
        showingChallengeInvites = true
        showingChallengeDetails = false
        showingCreateChallenge = false
        showingAchievements = false
        handleTabChange(3)
    }
    
    fun navigateToCreateChallenge() {
        showingCreateChallenge = true
        showingChallengeInvites = false
        showingChallengeDetails = false
        showingAchievements = false
        handleTabChange(3)
    }
    
    fun navigateToAchievements() {
        showingAchievements = true
        showingChallengeInvites = false
        showingChallengeDetails = false
        showingCreateChallenge = false
        handleTabChange(3)
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

                // Friends tab
                BottomNavigationItem(
                    selected = currentTab == 1,
                    onClick = {
                        handleTabChange(1)
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

                // Activity/Camera tab
                BottomNavigationItem(
                    selected = currentTab == 2,
                    onClick = {
                        handleTabChange(2)
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

                // Challenges tab
                BottomNavigationItem(
                    selected = currentTab == 3,
                    onClick = {
                        handleTabChange(3)
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
                
                // Profile tab
                BottomNavigationItem(
                    selected = currentTab == 4,
                    onClick = {
                        handleTabChange(4)
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
            if (currentTab == 4) {
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
            userScrollEnabled = !(currentTab == 2 && hasTakenPicture),
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
                1 -> {
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
                2 -> CameraScreen(
                    isVisible = currentTab == 2,
                    onAfterWorkoutModeChanged = { isAfterWorkoutMode ->
                        hasTakenPicture = isAfterWorkoutMode
                    }
                ).Content()
                3 -> {
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
                4 -> ProfileScreen().Content()
                
                else -> FeedScreen().Content()
            }
        }
    }
}
