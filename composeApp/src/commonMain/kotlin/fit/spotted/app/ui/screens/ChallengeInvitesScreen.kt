package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.ChallengeInvite
import fit.spotted.app.ui.components.PullToRefreshLayout
import fit.spotted.app.ui.components.SkeletonLoadingEffect
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ChallengeInvitesScreen {
    var onNavigateBack: () -> Unit = {}
    var onNavigateToChallengeDetails: (Int) -> Unit = {}
    
    // Helper function to format dates
    private fun formatDate(timestamp: Long): String {
        val date = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.month.name.take(3)} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    }
    
    @Composable
    fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val apiClient = ApiProvider.getApiClient()
        
        var invites by remember { mutableStateOf<List<ChallengeInvite>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var refreshing by remember { mutableStateOf(false) }
        
        // Get invites on first load
        LaunchedEffect(Unit) {
            try {
                invites = apiClient.getChallengeInvites().invites
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
        
        // Function to refresh invites
        fun refreshInvites() {
            coroutineScope.launch {
                refreshing = true
                try {
                    invites = apiClient.getChallengeInvites().invites
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    refreshing = false
                }
            }
        }
        
        // Function to respond to invite
        fun respondToInvite(challengeId: Int, accept: Boolean) {
            coroutineScope.launch {
                try {
                    apiClient.respondToChallengeInvite(challengeId, accept)
                    // Refresh the list after responding
                    refreshInvites()
                    
                    // If accepted, navigate to challenge details
                    if (accept) {
                        onNavigateToChallengeDetails(challengeId)
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Challenge Invites") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = MaterialTheme.colors.surface,
                    modifier = Modifier.padding(top = LocalAdaptiveSpacing.current.statusBarPadding)
                )
            }
        ) { paddingValues ->
            PullToRefreshLayout(
                isRefreshing = refreshing,
                onRefresh = { refreshInvites() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    InvitesSkeleton()
                } else if (invites.isNullOrEmpty()) {
                    EmptyInvitesList()
                } else {
                    InvitesList(
                        invites = invites!!,
                        onAccept = { invite -> respondToInvite(invite.challenge.id, true) },
                        onDecline = { invite -> respondToInvite(invite.challenge.id, false) }
                    )
                }
            }
        }
    }
    
    @Composable
    private fun InvitesList(
        invites: List<ChallengeInvite>,
        onAccept: (ChallengeInvite) -> Unit,
        onDecline: (ChallengeInvite) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(invites) { invite ->
                InviteCard(
                    invite = invite,
                    onAccept = { onAccept(invite) },
                    onDecline = { onDecline(invite) }
                )
            }
        }
    }
    
    @Composable
    private fun InviteCard(
        invite: ChallengeInvite,
        onAccept: () -> Unit,
        onDecline: () -> Unit
    ) {
        val now = Clock.System.now()
        val startDate = Instant.fromEpochMilliseconds(invite.challenge.startDate)
        val endDate = Instant.fromEpochMilliseconds(invite.challenge.endDate)
        val invitedAt = Instant.fromEpochMilliseconds(invite.invitedAt)
        
        val startDateFormatted = formatDate(invite.challenge.startDate)
        val endDateFormatted = formatDate(invite.challenge.endDate)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Invited by info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.primary)
                    ) {
                        Text(
                            text = invite.invitedBy.username.first().toString(),
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "${invite.invitedBy.username} invited you to a challenge",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Challenge info
                Text(
                    text = invite.challenge.name,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = invite.challenge.description,
                    style = MaterialTheme.typography.body2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Challenge period
                Row(
                    modifier = Modifier.fillMaxWidth()
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
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column {
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Target duration
                Text(
                    text = "Target: ${invite.challenge.targetDuration} minutes",
                    style = MaterialTheme.typography.body2
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Participants count
                Text(
                    text = "${invite.challenge.participants.size} participants already joined",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Accept/Decline buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDecline,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colors.error
                        )
                    ) {
                        Text("Decline")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
    
    @Composable
    private fun EmptyInvitesList() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Invites",
                    style = MaterialTheme.typography.h6,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "You don't have any pending challenge invites",
                    style = MaterialTheme.typography.body2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    
    @Composable
    private fun InvitesSkeleton() {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Invited by skeleton
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SkeletonLoadingEffect(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            SkeletonLoadingEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Challenge name skeleton
                        SkeletonLoadingEffect(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Description skeleton
                        SkeletonLoadingEffect(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        SkeletonLoadingEffect(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Date skeletons
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                SkeletonLoadingEffect(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                SkeletonLoadingEffect(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(24.dp))
                            
                            Column {
                                SkeletonLoadingEffect(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                SkeletonLoadingEffect(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Buttons skeleton
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            SkeletonLoadingEffect(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            SkeletonLoadingEffect(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
} 