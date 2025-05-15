package fit.spotted.app.ui.datepicker

import androidx.compose.runtime.Composable

/**
 * Platform-specific date picker implementation
 * On Android, this will show the system date picker
 * 
 * @param initialDate The initial date to show in the picker (Unix timestamp in ms)
 * @param onDateSelected Callback that will be called when a date is selected
 * @param onDismiss Callback that will be called when the date picker is dismissed without selection
 */
@Composable
expect fun PlatformDatePicker(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit = {}
) 