package fit.spotted.app.emoji

import org.kodein.emoji.Emoji
import org.kodein.emoji.activities.sport.Basketball
import org.kodein.emoji.activities.sport.BoxingGlove
import org.kodein.emoji.people_body.person_activity.Running
import org.kodein.emoji.people_body.person_sport.Skier
import org.kodein.emoji.people_body.person_sport.Swimming
import org.kodein.emoji.travel_places.transport_ground.Bicycle

/**
 * Represents different types of physical activities that can be tracked in the app.
 * 
 * @property emoji The Unicode emoji character representing the activity
 * @property emojiWasm The Emoji object for WASM platform compatibility
 */
enum class ActivityType(val emoji: String, val emojiWasm: Emoji) {
    RUNNING("🏃", Emoji.Running),
    CYCLING("🚲", Emoji.Bicycle),
    SWIMMING("🏊", Emoji.Swimming),
    SKIING("⛷️", Emoji.Skier),
    BOXING("🥊", Emoji.BoxingGlove),
    BASKETBALL("🏀", Emoji.Basketball)
}
