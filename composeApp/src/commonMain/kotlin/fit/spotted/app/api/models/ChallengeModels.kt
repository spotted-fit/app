package fit.spotted.app.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: Int,
    val name: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val targetDuration: Int, // Target workout time in minutes
    val currentProgress: Int, // Current total workout time in minutes
    val participants: List<ChallengeParticipant>,
    val createdBy: UserInfo,
    val isCompleted: Boolean = false
)

@Serializable
data class ChallengeParticipant(
    val user: UserInfo,
    val contributedMinutes: Int,
    val joinedAt: Long
)

@Serializable
data class CreateChallengeRequest(
    val name: String,
    val description: String? = null,
    val startDate: Long,
    val endDate: Long,
    val targetDuration: Int,
    val invitedUsernames: List<String>
)

@Serializable
data class ChallengeResponse(
    val challenge: Challenge
)

@Serializable
data class ChallengesList(
    val challenges: List<Challenge>
)

@Serializable
data class ChallengeInvite(
    val id: Int,
    val challenge: Challenge,
    val invitedBy: UserInfo,
    val invitedAt: Long
)

@Serializable
data class ChallengeInvitesList(
    val invites: List<ChallengeInvite>
)

@Serializable
data class ChallengeInviteResponse(
    val challengeId: Int,
    val accepted: Boolean
)

@Serializable
data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedAt: Long,
    val challengeId: Int?
)

@Serializable
data class AchievementsList(
    val achievements: List<Achievement>
) 