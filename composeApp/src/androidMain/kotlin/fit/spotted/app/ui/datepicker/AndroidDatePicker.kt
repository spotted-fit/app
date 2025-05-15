package fit.spotted.app.ui.datepicker

import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Date

/**
 * Shows the Android system date picker dialog
 * 
 * @param initialDate The initial date to show in the picker
 * @param onDateSelected Callback that will be called when a date is selected
 * @param onDismiss Callback that will be called when the date picker is dismissed without selection
 */
@Composable
actual fun PlatformDatePicker(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialDate
    
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(selectedCalendar.timeInMillis)
        },
        year,
        month,
        day
    )
    
    // Remove the minimum date restriction to allow selecting today
    /*
    // Set minimum date to today
    val today = Calendar.getInstance()
    datePickerDialog.datePicker.minDate = today.timeInMillis
    */
    
    // Set dismiss listener
    datePickerDialog.setOnCancelListener {
        onDismiss()
    }
    
    datePickerDialog.show()
} 