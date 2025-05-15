package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.Challenge
import fit.spotted.app.ui.components.PullToRefreshLayout
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class ChallengesScreen {
    // Navigation callbacks
    var onNavigateToCreateChallenge: () -> Unit = {}
    var onNavigateToChallengeInvites: () -> Unit = {}
    var onNavigateToChallengeDetails: (Int) -> Unit = {}
    var onNavigateToAchievements: () -> Unit = {}

    @Composable
    fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val apiClient = ApiProvider.getApiClient()
        
        var challenges by remember { mutableStateOf<List<Challenge>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var hasInvites by remember { mutableStateOf(false) }
        var refreshing by remember { mutableStateOf(false) }
        
        // Get challenges on first load
        LaunchedEffect(Unit) {
            try {
                challenges = apiClient.getChallenges().challenges
                val invites = apiClient.getChallengeInvites().invites
                hasInvites = invites.isNotEmpty()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
        
        // Function to refresh challenges
        fun refreshChallenges() {
            coroutineScope.launch {
                refreshing = true
                try {
                    challenges = apiClient.getChallenges().challenges
                    val invites = apiClient.getChallengeInvites().invites
                    hasInvites = invites.isNotEmpty()
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    refreshing = false
                }
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Challenges") },
                    backgroundColor = MaterialTheme.colors.surface,
                    actions = {
                        // Badge to show if there are invites
                        if (hasInvites) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text("!")
                                    }
                                }
                            ) {
                                IconButton(onClick = onNavigateToChallengeInvites) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Challenge Invites"
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = onNavigateToChallengeInvites) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Challenge Invites"
                                )
                            }
                        }
                        
                        // Button to create a new challenge
                        IconButton(onClick = onNavigateToCreateChallenge) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create Challenge"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigateToAchievements() },
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    Text("🏆", fontSize = 20.sp)
                }
            }
        ) { paddingValues ->
            PullToRefreshLayout(
                isRefreshing = refreshing,
                onRefresh = { refreshChallenges() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    LoadingSkeleton()
                } else {
                    if (challenges.isNullOrEmpty()) {
                        EmptyChallengeList(onNavigateToCreateChallenge)
                    } else {
                        ChallengeList(
                            challenges = challenges!!,
                            onChallengeClick = { challenge -> onNavigateToChallengeDetails(challenge.id) }
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    private fun ChallengeList(
        challenges: List<Challenge>,
        onChallengeClick: (Challenge) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    onClick = { onChallengeClick(challenge) }
                )
            }
        }
    }
    
    @Composable
    private fun ChallengeCard(
        challenge: Challenge,
        onClick: () -> Unit
    ) {
        val now = Clock.System.now()
        val startDate = Instant.fromEpochMilliseconds(challenge.startDate)
        val endDate = Instant.fromEpochMilliseconds(challenge.endDate)
        
        val isActive = now >= startDate && now <= endDate
        val isUpcoming = now < startDate
        val isCompleted = challenge.isCompleted || now > endDate
        
        val progressPercentage = if (challenge.targetDuration > 0) {
            (challenge.currentProgress.toFloat() / challenge.targetDuration) * 100
        } else 0f
        
        val cardColor = when {
            isUpcoming -> Color(0xFF3F51B5)
            isActive -> Color(0xFF4CAF50)
            isCompleted -> Color(0xFF9E9E9E)
            else -> MaterialTheme.colors.surface
        }
        
        val gradientBrush = Brush.horizontalGradient(
            colors = listOf(
                cardColor,
                cardColor.copy(alpha = 0.8f)
            )
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(16.dp)
            ) {
                // Challenge title
                Text(
                    text = challenge.name,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Challenge description
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.body2,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = progressPercentage / 100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color.White,
                    backgroundColor = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${challenge.currentProgress} / ${challenge.targetDuration} minutes",
                        style = MaterialTheme.typography.caption,
                        color = Color.White
                    )
                    
                    Text(
                        text = "${progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.caption,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Status and time remaining
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when {
                        isUpcoming -> "Upcoming"
                        isActive -> "Active"
                        isCompleted -> "Completed"
                        else -> ""
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    // Time remaining calculation
                    val timeRemainingText = when {
                        isUpcoming -> {
                            val daysUntil = (startDate.toEpochMilliseconds() - now.toEpochMilliseconds()).div(86400000) + 1
                            "Starts in $daysUntil days"
                        }
                        isActive -> {
                            val daysLeft = (endDate.toEpochMilliseconds() - now.toEpochMilliseconds()).div(86400000) + 1
                            "$daysLeft days left"
                        }
                        else -> "Challenge ended"
                    }
                    
                    Text(
                        text = timeRemainingText,
                        style = MaterialTheme.typography.caption,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Participants count
                Text(
                    text = "${challenge.participants.size} participants",
                    style = MaterialTheme.typography.caption,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
    
    @Composable
    private fun EmptyChallengeList(onCreateClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Challenges Yet",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start a challenge with friends to track progress together!",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Challenge")
            }
        }
    }
    
    @Composable
    private fun LoadingSkeleton() {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                            )
                            
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                            )
                        }
                    }
                }
            }
        }
    }
} 