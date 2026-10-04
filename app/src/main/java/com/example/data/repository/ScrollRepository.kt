package com.example.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import com.example.data.local.ScrollSenseDao
import com.example.data.model.DailyPlatformData
import com.example.data.model.DailyScrollSummaryEntity
import com.example.data.model.PlatformBreakdown
import com.example.data.model.PlatformType
import com.example.data.model.ReelScrollEntity
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyScreenTimeReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScrollRepository(
    private val context: Context,
    private val dao: ScrollSenseDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("scrollsense_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())

    private val _userProfileFlow = MutableStateFlow(loadUserProfile())
    val userProfileFlow = _userProfileFlow.asStateFlow()

    private val recommendationEngine = FeedRecommendationEngine()

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        val count = dao.getTotalScrollEventsCount()
        if (count == 0) {
            seedInitialDemoData()
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun incrementScroll(platform: String, dateString: String, hour: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getDailySummary(dateString)
        val todayCal = Calendar.getInstance()
        val dayLabel = dayLabelFormat.format(todayCal.time)

        val updated = if (existing != null) {
            val isIg = platform.contains("Instagram")
            val isYt = platform.contains("YouTube")
            val isTt = platform.contains("TikTok")
            val isOther = !isIg && !isYt && !isTt

            existing.copy(
                totalReels = existing.totalReels + 1,
                instagramReels = existing.instagramReels + if (isIg) 1 else 0,
                youtubeShorts = existing.youtubeShorts + if (isYt) 1 else 0,
                tiktokReels = existing.tiktokReels + if (isTt) 1 else 0,
                otherReels = existing.otherReels + if (isOther) 1 else 0,
                // Roughly 30-45 seconds screen time per reel scroll
                socialScreenTimeMinutes = existing.socialScreenTimeMinutes + 1,
                totalScreenTimeMinutes = existing.totalScreenTimeMinutes + 1,
                peakHour = hour
            )
        } else {
            DailyScrollSummaryEntity(
                dateString = dateString,
                dayLabel = dayLabel,
                totalReels = 1,
                instagramReels = if (platform.contains("Instagram")) 1 else 0,
                youtubeShorts = if (platform.contains("YouTube")) 1 else 0,
                tiktokReels = if (platform.contains("TikTok")) 1 else 0,
                otherReels = 0,
                totalScreenTimeMinutes = 20,
                socialScreenTimeMinutes = 1,
                peakHour = hour,
                goalMet = true
            )
        }
        dao.insertDailySummary(updated)
    }

    fun observeWeeklySummaries(): Flow<List<DailyScrollSummaryEntity>> {
        return dao.getAllDailySummaries()
    }

    suspend fun getWeeklyReport(): WeeklyScreenTimeReport = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val todayStr = dateFormat.format(calendar.time)

        // Generate past 7 days
        val dailyList = mutableListOf<DailyPlatformData>()
        var totalReels = 0
        var totalMinutes = 0L

        var totalIgMins = 0L
        var totalYtMins = 0L
        var totalTtMins = 0L
        var totalOtherMins = 0L

        var igReels = 0
        var ytReels = 0
        var ttReels = 0
        var otherReels = 0

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startDateStr = dateFormat.format(cal.time)

        for (i in 0..6) {
            val dateStr = dateFormat.format(cal.time)
            val dayLabel = dayLabelFormat.format(cal.time)
            val isToday = (dateStr == todayStr)

            val summary = dao.getDailySummary(dateStr)

            val dayTotalReels = summary?.totalReels ?: (if (isToday) 14 else 42)
            val dayTotalMins = summary?.totalScreenTimeMinutes ?: (if (isToday) 65 else 180)
            val dayIgMins = (summary?.instagramReels?.toLong() ?: 18L) * 2
            val dayYtMins = (summary?.youtubeShorts?.toLong() ?: 12L) * 2
            val dayTtMins = (summary?.tiktokReels?.toLong() ?: 10L) * 2
            val dayOtherMins = (dayTotalMins - dayIgMins - dayYtMins - dayTtMins).coerceAtLeast(15)

            dailyList.add(
                DailyPlatformData(
                    dateString = dateStr,
                    dayLabel = dayLabel,
                    totalReels = dayTotalReels,
                    totalMinutes = dayTotalMins,
                    instagramMinutes = dayIgMins,
                    youtubeMinutes = dayYtMins,
                    tiktokMinutes = dayTtMins,
                    otherMinutes = dayOtherMins,
                    isToday = isToday
                )
            )

            totalReels += dayTotalReels
            totalMinutes += dayTotalMins
            totalIgMins += dayIgMins
            totalYtMins += dayYtMins
            totalTtMins += dayTtMins
            totalOtherMins += dayOtherMins

            igReels += summary?.instagramReels ?: 18
            ytReels += summary?.youtubeShorts ?: 14
            ttReels += summary?.tiktokReels ?: 10
            otherReels += summary?.otherReels ?: 5

            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val platformMap = mutableMapOf<String, PlatformBreakdown>()
        val totalSocialMins = (totalIgMins + totalYtMins + totalTtMins + totalOtherMins).coerceAtLeast(1)

        platformMap["Instagram Reels"] = PlatformBreakdown(
            platformName = "Instagram Reels",
            reelCount = igReels,
            screenTimeMinutes = totalIgMins,
            percentage = (totalIgMins.toFloat() / totalSocialMins.toFloat())
        )
        platformMap["YouTube Shorts"] = PlatformBreakdown(
            platformName = "YouTube Shorts",
            reelCount = ytReels,
            screenTimeMinutes = totalYtMins,
            percentage = (totalYtMins.toFloat() / totalSocialMins.toFloat())
        )
        platformMap["TikTok"] = PlatformBreakdown(
            platformName = "TikTok",
            reelCount = ttReels,
            screenTimeMinutes = totalTtMins,
            percentage = (totalTtMins.toFloat() / totalSocialMins.toFloat())
        )
        platformMap["Other Apps"] = PlatformBreakdown(
            platformName = "Other Apps",
            reelCount = otherReels,
            screenTimeMinutes = totalOtherMins,
            percentage = (totalOtherMins.toFloat() / totalSocialMins.toFloat())
        )

        val topPlatform = platformMap.maxByOrNull { it.value.reelCount }?.key ?: "Instagram Reels"

        WeeklyScreenTimeReport(
            startDate = startDateStr,
            endDate = todayStr,
            totalReelsScrolled = totalReels,
            totalScreenTimeMinutes = totalMinutes,
            averageDailyReels = (totalReels / 7),
            averageDailyMinutes = (totalMinutes / 7),
            topPlatform = topPlatform,
            peakDoomscrollTime = "10:30 PM - 12:00 AM",
            weekComparisonPercent = -12, // 12% improvement
            dailyBreakdowns = dailyList,
            platformTotals = platformMap
        )
    }

    fun getUserProfile(): UserProfile {
        return _userProfileFlow.value
    }

    fun updateUserProfile(profile: UserProfile) {
        prefs.edit()
            .putInt("age", profile.age)
            .putString("gender", profile.gender)
            .putInt("daily_reel_limit", profile.dailyReelLimit)
            .putInt("daily_screentime_limit", profile.dailyScreenTimeLimitMinutes)
            .putBoolean("nudge_enabled", profile.nudgeNotificationEnabled)
            .apply()
        _userProfileFlow.value = profile
    }

    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            age = prefs.getInt("age", 22),
            gender = prefs.getString("gender", "Male") ?: "Male",
            dailyReelLimit = prefs.getInt("daily_reel_limit", 30),
            dailyScreenTimeLimitMinutes = prefs.getInt("daily_screentime_limit", 60),
            nudgeNotificationEnabled = prefs.getBoolean("nudge_enabled", true)
        )
    }

    fun getRecommendedFeeds(profile: UserProfile) = recommendationEngine.getRecommendedFeeds(profile)

    fun getDetoxPlanSummary(profile: UserProfile) = recommendationEngine.getDetoxPlanSummary(profile)

    suspend fun simulateReelScroll(platform: PlatformType) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val todayStr = dateFormat.format(Date(now))
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        dao.insertScrollEvent(
            ReelScrollEntity(
                platform = platform.displayName,
                packageName = platform.packageName,
                timestamp = now,
                dateString = todayStr,
                hourOfDay = hour,
                dayOfWeek = dayOfWeek,
                scrollDelta = 1,
                durationSeconds = 20
            )
        )
        incrementScroll(platform.displayName, todayStr, hour)
    }

    private suspend fun seedInitialDemoData() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val summaries = mutableListOf<DailyScrollSummaryEntity>()

        val baseReels = listOf(48, 62, 54, 38, 75, 82, 35)
        val baseScreenMinutes = listOf(210L, 260L, 230L, 180L, 310L, 340L, 160L)

        for (i in 0..6) {
            val dateStr = dateFormat.format(cal.time)
            val dayLabel = dayLabelFormat.format(cal.time)
            val totalR = baseReels[i]
            val totalM = baseScreenMinutes[i]

            val ig = (totalR * 0.45).toInt()
            val yt = (totalR * 0.30).toInt()
            val tt = (totalR * 0.20).toInt()
            val ot = totalR - ig - yt - tt

            summaries.add(
                DailyScrollSummaryEntity(
                    dateString = dateStr,
                    dayLabel = dayLabel,
                    totalReels = totalR,
                    instagramReels = ig,
                    youtubeShorts = yt,
                    tiktokReels = tt,
                    otherReels = ot,
                    totalScreenTimeMinutes = totalM,
                    socialScreenTimeMinutes = (totalR * 1.5).toLong(),
                    peakHour = if (i % 2 == 0) 22 else 23,
                    goalMet = totalR <= 40
                )
            )

            // Insert a few individual reel events for realism
            for (r in 0 until (totalR.coerceAtMost(8))) {
                dao.insertScrollEvent(
                    ReelScrollEntity(
                        platform = if (r % 2 == 0) "Instagram Reels" else "YouTube Shorts",
                        packageName = if (r % 2 == 0) "com.instagram.android" else "com.google.android.youtube",
                        timestamp = cal.timeInMillis + (r * 3600000L),
                        dateString = dateStr,
                        hourOfDay = 21 + (r % 3),
                        dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
                        scrollDelta = 1,
                        durationSeconds = 18
                    )
                )
            }

            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        dao.insertDailySummaries(summaries)
    }
}
