package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import fit.spotted.app.api.models.Achievement
import fit.spotted.app.ui.components.PullToRefreshLayout
import fit.spotted.app.ui.components.SkeletonLoadingEffect
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AchievementsScreen {
    var onNavigateBack: () -> Unit = {}
    var onNavigateToChallenge: (Int) -> Unit = {}
    
    @Composable
    fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val apiClient = ApiProvider.getApiClient()
        
        var achievements by remember { mutableStateOf<List<Achievement>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var refreshing by remember { mutableStateOf(false) }
        var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
        
        // Get achievements on first load
        LaunchedEffect(Unit) {
            try {
                achievements = apiClient.getAchievements().achievements
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
        
        // Function to refresh achievements
        fun refreshAchievements() {
            coroutineScope.launch {
                refreshing = true
                try {
                    achievements = apiClient.getAchievements().achievements
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
                    title = { Text("Achievements") },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PullToRefreshLayout(
                    isRefreshing = refreshing,
                    onRefresh = { refreshAchievements() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading) {
                        AchievementsSkeleton()
                    } else if (achievements.isNullOrEmpty()) {
                        EmptyAchievements()
                    } else {
                        AchievementsGrid(
                            achievements = achievements!!,
                            onAchievementClick = { achievement ->
                                selectedAchievement = achievement
                            }
                        )
                    }
                }
                
                // Show achievement details dialog
                if (selectedAchievement != null) {
                    AchievementDetailsDialog(
                        achievement = selectedAchievement!!,
                        onDismiss = { selectedAchievement = null },
                        onNavigateToChallenge = { challengeId ->
                            selectedAchievement = null
                            onNavigateToChallenge(challengeId)
                        }
                    )
                }
            }
        }
    }
    
    @Composable
    private fun AchievementsGrid(
        achievements: List<Achievement>,
        onAchievementClick: (Achievement) -> Unit
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(achievements) { achievement ->
                AchievementItem(
                    achievement = achievement,
                    onClick = { onAchievementClick(achievement) }
                )
            }
        }
    }
    
    @Composable
    private fun AchievementItem(
        achievement: Achievement,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clickable { onClick() },
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Background gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.2f),
                                    Color(0xFFFFD700).copy(alpha = 0.05f)
                                )
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Achievement icon/emoji
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFD700).copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Here we're just showing a medal emoji, but you could load an image from iconUrl
                        Text(
                            text = "🏆",
                            fontSize = 36.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Achievement name
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Date earned - using Kotlin Multiplatform
                    val earnedDate = Instant.fromEpochMilliseconds(achievement.earnedAt)
                    val localDateTime = earnedDate.toLocalDateTime(TimeZone.currentSystemDefault())
                    val earnedDateText = "${localDateTime.month.name} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
                    
                    Text(
                        text = "Earned on $earnedDateText",
                        style = MaterialTheme.typography.caption,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
    
    @Composable
    private fun AchievementDetailsDialog(
        achievement: Achievement,
        onDismiss: () -> Unit,
        onNavigateToChallenge: (Int) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    // Format earned date using our Kotlin Multiplatform approach
                    val earnedDate = Instant.fromEpochMilliseconds(achievement.earnedAt)
                    val localDateTime = earnedDate.toLocalDateTime(TimeZone.currentSystemDefault())
                    val earnedDateText = "${localDateTime.month.name} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
                    
                    Text(
                        text = "Earned on $earnedDateText",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    if (achievement.challengeId != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "This achievement was earned by completing a challenge.",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            },
            confirmButton = {
                if (achievement.challengeId != null) {
                    Button(
                        onClick = { onNavigateToChallenge(achievement.challengeId) }
                    ) {
                        Text("View Challenge")
                    }
                } else {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                if (achievement.challengeId != null) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }
    
    @Composable
    private fun EmptyAchievements() {
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
                    text = "🏅",
                    fontSize = 48.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Achievements Yet",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Complete challenges to earn achievements and medals for your profile!",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    
    @Composable
    private fun AchievementsSkeleton() {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(6) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon skeleton
                        SkeletonLoadingEffect(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Name skeleton
                        SkeletonLoadingEffect(
                            modifier = Modifier
                                .width(100.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Date skeleton
                        SkeletonLoadingEffect(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
} 