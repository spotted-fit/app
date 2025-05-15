package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.Challenge
import fit.spotted.app.api.models.ChallengeParticipant
import fit.spotted.app.ui.components.SkeletonLoadingEffect
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ChallengeDetailsScreen(private val challengeId: Int) {
    var onNavigateBack: () -> Unit = {}
    var onNavigateToUserProfile: (String) -> Unit = {}
    
    @Composable
    fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val apiClient = ApiProvider.getApiClient()
        
        var challenge by remember { mutableStateOf<Challenge?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var showLeaveDialog by remember { mutableStateOf(false) }
        
        // Load challenge details
        LaunchedEffect(challengeId) {
            try {
                challenge = apiClient.getChallenge(challengeId).challenge
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
        
        // Handle leaving challenge
        fun leaveChallenge() {
            coroutineScope.launch {
                try {
                    apiClient.leaveChallenge(challengeId)
                    onNavigateBack()
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
        
        // Leave confirmation dialog
        if (showLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                title = { Text("Leave Challenge") },
                text = { Text("Are you sure you want to leave this challenge? Your progress will remain, but you won't be able to rejoin.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLeaveDialog = false
                            leaveChallenge()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red
                        )
                    ) {
                        Text("Leave", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(challenge?.name ?: "Challenge Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = MaterialTheme.colors.surface,
                    modifier = Modifier.padding(top = LocalAdaptiveSpacing.current.statusBarPadding),
                    actions = {
                        TextButton(
                            onClick = { showLeaveDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Leave")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else if (challenge == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Challenge not found")
                }
            } else {
                ChallengeDetailsContent(
                    challenge = challenge!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onUserClick = onNavigateToUserProfile
                )
            }
        }
    }
    
    @Composable
    private fun ChallengeDetailsContent(
        challenge: Challenge,
        modifier: Modifier = Modifier,
        onUserClick: (String) -> Unit
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
        
        val headerColor = when {
            isUpcoming -> Color(0xFF3F51B5)
            isActive -> Color(0xFF4CAF50)
            isCompleted -> Color(0xFF9E9E9E)
            else -> MaterialTheme.colors.primary
        }
        
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                headerColor,
                headerColor.copy(alpha = 0.8f),
                MaterialTheme.colors.background
            )
        )
        
        // Format dates for display using Kotlin Multiplatform
        val startLocalDateTime = startDate.toLocalDateTime(TimeZone.currentSystemDefault())
        val endLocalDateTime = endDate.toLocalDateTime(TimeZone.currentSystemDefault())
        
        val startDateFormatted = "${startLocalDateTime.month.name.take(3)} ${startLocalDateTime.dayOfMonth}, ${startLocalDateTime.year}"
        val endDateFormatted = "${endLocalDateTime.month.name.take(3)} ${endLocalDateTime.dayOfMonth}, ${endLocalDateTime.year}"
        
        // Sort participants by contribution
        val sortedParticipants = challenge.participants.sortedByDescending { it.contributedMinutes }
        
        LazyColumn(
            modifier = modifier
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(gradientBrush)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = challenge.name,
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.body2,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Status badge
                        val statusText = when {
                            isUpcoming -> "Upcoming"
                            isActive -> "Active"
                            isCompleted -> "Completed"
                            else -> ""
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Challenge period
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Challenge Period",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Start Date",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Text(
                                        text = startDateFormatted,
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "End Date",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                    
                                    Text(
                                        text = endDateFormatted,
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Progress section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Challenge Progress",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${progressPercentage.toInt()}%",
                                    style = MaterialTheme.typography.h4,
                                    fontWeight = FontWeight.Bold,
                                    color = headerColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LinearProgressIndicator(
                                progress = progressPercentage / 100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = headerColor,
                                backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current: ${challenge.currentProgress} minutes",
                                    style = MaterialTheme.typography.caption
                                )
                                
                                Text(
                                    text = "Target: ${challenge.targetDuration} minutes",
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Created by section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Created by",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colors.primary)
                                ) {
                                    Text(
                                        text = challenge.createdBy.username.first().toString(),
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Center),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = challenge.createdBy.username,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Leaderboard
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            items(sortedParticipants) { participant ->
                LeaderboardItem(
                    participant = participant,
                    position = sortedParticipants.indexOf(participant) + 1,
                    onUserClick = { onUserClick(participant.user.username) }
                )
            }
            
            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    @Composable
    private fun LeaderboardItem(
        participant: ChallengeParticipant,
        position: Int,
        onUserClick: () -> Unit
    ) {
        val backgroundColor = when (position) {
            1 -> Color(0xFFFFC107).copy(alpha = 0.2f) // Gold
            2 -> Color(0xFFB0BEC5).copy(alpha = 0.2f) // Silver
            3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Bronze
            else -> MaterialTheme.colors.surface
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 1.dp,
            backgroundColor = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Position
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp)
                )
                
                // User avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary)
                        .clickable { onUserClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.user.username.first().toString(),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Username
                Text(
                    text = participant.user.username,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserClick() }
                )
                
                // Minutes contributed
                Text(
                    text = "${participant.contributedMinutes} min",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 