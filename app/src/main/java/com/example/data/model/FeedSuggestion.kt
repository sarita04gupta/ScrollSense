package com.example.data.model

data class UserProfile(
    val age: Int = 22,
    val gender: String = "Male", // "Male", "Female", "Non-binary", "Prefer not to say"
    val dailyReelLimit: Int = 30, // max reels threshold
    val dailyScreenTimeLimitMinutes: Int = 60,
    val nudgeNotificationEnabled: Boolean = true,
    val primaryInterests: List<String> = listOf("Coding & Tech", "Fitness & Calisthenics", "Financial Freedom", "Science & Space")
)

data class FeedSuggestion(
    val id: String,
    val title: String,
    val creatorOrHandle: String,
    val platform: String, // "YouTube", "Podcast", "Substack", "Instagram", "Reddit"
    val category: String, // e.g. "Mental Focus & Stoicism", "Tech & AI", "Health & Wellness", "Career & Wealth", "Creativity & Art"
    val description: String,
    val whyRecommended: String,
    val targetGenders: List<String> = listOf("All"),
    val minAge: Int = 13,
    val maxAge: Int = 99,
    val actionUrl: String,
    val isSaved: Boolean = false,
    val badge: String = "High Retention"
)

data class WeeklyScreenTimeReport(
    val startDate: String,
    val endDate: String,
    val totalReelsScrolled: Int,
    val totalScreenTimeMinutes: Long,
    val averageDailyReels: Int,
    val averageDailyMinutes: Long,
    val topPlatform: String,
    val peakDoomscrollTime: String,
    val weekComparisonPercent: Int, // e.g. -14% vs last week
    val dailyBreakdowns: List<DailyPlatformData>,
    val platformTotals: Map<String, PlatformBreakdown>
)

data class DailyPlatformData(
    val dateString: String,
    val dayLabel: String,
    val totalReels: Int,
    val totalMinutes: Long,
    val instagramMinutes: Long,
    val youtubeMinutes: Long,
    val tiktokMinutes: Long,
    val otherMinutes: Long,
    val isToday: Boolean = false
)

data class PlatformBreakdown(
    val platformName: String,
    val reelCount: Int,
    val screenTimeMinutes: Long,
    val percentage: Float
)
