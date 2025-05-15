package fit.spotted.app.ui.datepicker

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

/**
 * Placeholder implementation for web
 * In a real implementation, this would use a web date picker
 */
@Composable
actual fun PlatformDatePicker(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // For simplicity in this example, we'll show a dialog with preset dates
    // In a real implementation, this would use the web date picker API
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.h6
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        val tomorrow = Clock.System.now().plus(1.days).toEpochMilliseconds()
                        onDateSelected(tomorrow)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tomorrow")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        val nextWeek = Clock.System.now().plus(7.days).toEpochMilliseconds()
                        onDateSelected(nextWeek)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Week")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        val nextMonth = Clock.System.now().plus(30.days).toEpochMilliseconds()
                        onDateSelected(nextMonth)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Month")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
} 