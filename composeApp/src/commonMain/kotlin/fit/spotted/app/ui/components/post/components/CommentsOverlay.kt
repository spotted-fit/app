package fit.spotted.app.ui.components.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fit.spotted.app.api.models.CommentData

/**
 * Component that displays the comments overlay.
 */
@Composable
fun CommentsOverlay(
    comments: List<CommentData>,
    onAddComment: ((text: String) -> Unit)? = null,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(24.dp)
    ) {
        Column {
            // Header with title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                
                // Close button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            
            // Comments list
            comments.forEach { comment ->
                CommentItem(comment = comment)
            }
            
            // Add comment input field
            if (onAddComment != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CommentInput(onAddComment = onAddComment)
            }
        }
    }
}

/**
 * Component that displays a single comment.
 */
@Composable
private fun CommentItem(comment: CommentData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // User avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.username.first().toString(),
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Comment content
        Column {
            Text(
                text = comment.username,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Component that displays the comment input field.
 */
@Composable
private fun CommentInput(onAddComment: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text field for comment
        TextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Add a comment...", color = Color.White.copy(alpha = 0.6f)) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White.copy(alpha = 0.1f),
                cursorColor = Color.White,
                textColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Submit button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.primary)
                .clickable {
                    if (commentText.isNotBlank()) {
                        onAddComment(commentText)
                        commentText = ""
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "→",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}