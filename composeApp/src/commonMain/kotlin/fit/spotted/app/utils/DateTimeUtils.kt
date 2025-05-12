package fit.spotted.app.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility class for date and time operations.
 */
object DateTimeUtils {

    /**
     * Formats a timestamp in Instagram-style relative time format.
     *
     * @param timestamp The timestamp in milliseconds
     * @return Formatted string in Instagram style (e.g., "just now", "5m ago", "2h ago", "3d ago", "2w ago", or "Jan 15")
     */
    fun formatInstagramStyle(timestamp: Long): String {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val duration = now - instant

        // Convert to seconds, minutes, hours, days
        val seconds = duration.inWholeSeconds
        val minutes = duration.inWholeMinutes
        val hours = duration.inWholeHours
        val days = duration.inWholeDays

        return when {
            seconds < 60 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 30 -> "${days / 7}w ago"
            else -> {
                // For older posts, show month and day
                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                val month = localDateTime.month.name.take(3)
                val day = localDateTime.dayOfMonth
                "$month $day"
            }
        }
    }
}