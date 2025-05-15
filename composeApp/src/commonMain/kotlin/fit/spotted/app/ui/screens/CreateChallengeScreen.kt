package fit.spotted.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.ApiProvider
import fit.spotted.app.api.models.CreateChallengeRequest
import fit.spotted.app.api.models.UserInfo
import fit.spotted.app.ui.datepicker.PlatformDatePicker
import fit.spotted.app.ui.theme.LocalAdaptiveSpacing
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class CreateChallengeScreen {
    var onNavigateBack: () -> Unit = {}
    var onChallengeCreated: (Int) -> Unit = {}
    
    // Helper function to format dates
    private fun formatDate(timestamp: Long): String {
        val date = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.month.name.take(3)} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    }
    
    @OptIn(FlowPreview::class)
    @Composable
    fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val apiClient = ApiProvider.getApiClient()
        
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var targetDuration by remember { mutableStateOf("60") }
        
        // Set start date to tomorrow and end date to a week later
        val now = Clock.System.now()
        var startDate by remember { mutableStateOf(now.toEpochMilliseconds()) }
        var endDate by remember { mutableStateOf(now.plus(7.days).toEpochMilliseconds()) }
        
        var searchQuery by remember { mutableStateOf("") }
        val searchQueryFlow = remember { MutableStateFlow("") }
        var searchResults by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
        var isSearching by remember { mutableStateOf(false) }
        var selectedFriends by remember { mutableStateOf<List<UserInfo>>(emptyList()) }
        
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }
        var isCreating by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        // Set up search debounce
        LaunchedEffect(Unit) {
            searchQueryFlow
                .debounce(300)
                .collect { query ->
                    if (query.length >= 2) {
                        isSearching = true
                        try {
                            val searchResponse = apiClient.searchUsers(query)
                            // Filter out users that are already selected
                            if (searchResponse.response != null) {
                                searchResults = searchResponse.response.filter { user -> 
                                    !selectedFriends.any { it.id == user.id } 
                                }
                            }
                        } catch (e: Exception) {
                            // Handle error
                        } finally {
                            isSearching = false
                        }
                    } else {
                        searchResults = emptyList()
                        isSearching = false
                    }
                }
        }
        
        // Handle search query updates
        LaunchedEffect(searchQuery) {
            searchQueryFlow.emit(searchQuery)
        }
        
        // Function to create challenge
        fun createChallenge() {
            if (name.isBlank()) {
                errorMessage = "Please enter a challenge name"
                return
            }
            
            val targetDurationNumber = targetDuration.toIntOrNull()
            if (targetDurationNumber == null || targetDurationNumber <= 0) {
                errorMessage = "Please enter a valid target duration"
                return
            }
            
            if (endDate <= startDate) {
                errorMessage = "End date must be after start date"
                return
            }
            
            coroutineScope.launch {
                isCreating = true
                errorMessage = null
                
                try {
                    val request = CreateChallengeRequest(
                        name = name,
                        description = if (description.isBlank()) null else description,
                        startDate = startDate,
                        endDate = endDate,
                        targetDuration = targetDurationNumber,
                        invitedUsernames = selectedFriends.map { it.username }
                    )
                    
                    val result = apiClient.createChallenge(request)
                    onChallengeCreated(result.challenge.id)
                } catch (e: Exception) {
                    errorMessage = "Failed to create challenge: ${e.message}"
                } finally {
                    isCreating = false
                }
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Create Challenge") },
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
                // Main content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Challenge name
                    item {
                        Text(
                            text = "Challenge Name",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("E.g., 'Summer Fitness Challenge'") },
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Challenge description
                    item {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Describe your challenge...") },
                            minLines = 3,
                            maxLines = 5
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Challenge dates
                    item {
                        Text(
                            text = "Challenge Period",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Start date
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Start Date",
                                    style = MaterialTheme.typography.caption
                                )
                                
                                OutlinedTextField(
                                    value = formatDate(startDate),
                                    onValueChange = { /* Read only */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showStartDatePicker = true },
                                    trailingIcon = {
                                        IconButton(onClick = { showStartDatePicker = true }) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                        }
                                    },
                                    readOnly = true
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // End date
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "End Date",
                                    style = MaterialTheme.typography.caption
                                )
                                
                                OutlinedTextField(
                                    value = formatDate(endDate),
                                    onValueChange = { /* Read only */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showEndDatePicker = true },
                                    trailingIcon = {
                                        IconButton(onClick = { showEndDatePicker = true }) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                                        }
                                    },
                                    readOnly = true
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Target duration
                    item {
                        Text(
                            text = "Target Duration (minutes)",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = targetDuration,
                            onValueChange = { targetDuration = it.filter { char -> char.isDigit() } },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("E.g., 60") }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Invite friends
                    item {
                        Text(
                            text = "Invite Friends",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search for friends...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Selected friends
                    item {
                        if (selectedFriends.isNotEmpty()) {
                            Text(
                                text = "Selected Friends",
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedFriends) { friend ->
                                    SelectedFriendChip(
                                        friend = friend,
                                        onRemove = {
                                            selectedFriends = selectedFriends.filter { it.id != friend.id }
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Search results
                    if (searchQuery.length >= 2) {
                        if (isSearching) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        } else if (searchResults.isEmpty()) {
                            item {
                                Text(
                                    text = "No results found",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            items(searchResults) { user ->
                                UserSearchItem(
                                    user = user,
                                    onClick = {
                                        selectedFriends = selectedFriends + user
                                        searchQuery = ""
                                        searchResults = emptyList()
                                    }
                                )
                            }
                        }
                    }
                    
                    // Error message
                    if (errorMessage != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Create button
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { createChallenge() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isCreating
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colors.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Create Challenge")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                
                // Show date picker dialogs
                if (showStartDatePicker) {
                    PlatformDatePicker(
                        initialDate = startDate,
                        onDateSelected = { date ->
                            startDate = date
                            showStartDatePicker = false
                        },
                        onDismiss = { showStartDatePicker = false }
                    )
                }
                
                if (showEndDatePicker) {
                    PlatformDatePicker(
                        initialDate = endDate,
                        onDateSelected = { date ->
                            endDate = date
                            showEndDatePicker = false
                        },
                        onDismiss = { showEndDatePicker = false }
                    )
                }
            }
        }
    }
    
    @Composable
    private fun UserSearchItem(
        user: UserInfo,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.first().toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Username
            Text(
                text = user.username,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    @Composable
    private fun SelectedFriendChip(
        friend: UserInfo,
        onRemove: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.username.first().toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = friend.username,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
} 