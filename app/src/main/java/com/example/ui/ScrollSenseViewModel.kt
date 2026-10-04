package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.ScrollSenseApplication
import com.example.data.model.FeedSuggestion
import com.example.data.model.PlatformType
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyScreenTimeReport
import com.example.service.ScrollTrackerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ScrollSenseViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ScrollSenseApplication
    private val repository = app.repository

    val liveReelsToday: StateFlow<Int> = ScrollTrackerState.liveReelsToday

    val isServiceConnected: StateFlow<Boolean> = ScrollTrackerState.isServiceConnected

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow

    private val _weeklyReport = MutableStateFlow<WeeklyScreenTimeReport?>(null)
    val weeklyReport: StateFlow<WeeklyScreenTimeReport?> = _weeklyReport.asStateFlow()

    private val _recommendedFeeds = MutableStateFlow<List<FeedSuggestion>>(emptyList())
    val recommendedFeeds: StateFlow<List<FeedSuggestion>> = _recommendedFeeds.asStateFlow()

    private val _detoxPlanSummary = MutableStateFlow("")
    val detoxPlanSummary: StateFlow<String> = _detoxPlanSummary.asStateFlow()

    private val _hasUsageStatsPermission = MutableStateFlow(false)
    val hasUsageStatsPermission: StateFlow<Boolean> = _hasUsageStatsPermission.asStateFlow()

    private val _aiRecommendationAdvice = MutableStateFlow<String?>(null)
    val aiRecommendationAdvice: StateFlow<String?> = _aiRecommendationAdvice.asStateFlow()

    private val _isLoadingAi = MutableStateFlow(false)
    val isLoadingAi: StateFlow<Boolean> = _isLoadingAi.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    init {
        refreshWeeklyReport()
        refreshPermissions()
        refreshRecommendations(userProfile.value)
    }

    fun refreshPermissions() {
        _hasUsageStatsPermission.value = repository.hasUsageStatsPermission()
    }

    fun refreshWeeklyReport() {
        viewModelScope.launch {
            val report = repository.getWeeklyReport()
            _weeklyReport.value = report
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        repository.updateUserProfile(profile)
        refreshRecommendations(profile)
    }

    private fun refreshRecommendations(profile: UserProfile) {
        _recommendedFeeds.value = repository.getRecommendedFeeds(profile)
        _detoxPlanSummary.value = repository.getDetoxPlanSummary(profile)
    }

    fun simulateReelScroll(platform: PlatformType) {
        viewModelScope.launch {
            repository.simulateReelScroll(platform)
            val current = ScrollTrackerState.liveReelsToday.value + 1
            ScrollTrackerState.onReelScrolled(platform.displayName, current)
            refreshWeeklyReport()
        }
    }

    fun requestAiPersonalizedAnalysis() {
        val currentProfile = userProfile.value
        val report = weeklyReport.value
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Throwable) {
            ""
        }

        viewModelScope.launch {
            _isLoadingAi.value = true
            if (apiKey.isNullOrBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
                // High-intelligence local algorithmic detox prescription
                _aiRecommendationAdvice.value = buildLocalAiAdvice(currentProfile, report)
                _isLoadingAi.value = false
            } else {
                try {
                    val prompt = """
                        You are a neurobiology and digital wellness advisor. 
                        User Profile:
                        - Age: ${currentProfile.age}
                        - Gender: ${currentProfile.gender}
                        - Total Weekly Reels Scrolled: ${report?.totalReelsScrolled ?: 280}
                        - Top Platform: ${report?.topPlatform ?: "Instagram Reels"}
                        - Peak Doomscrolling: ${report?.peakDoomscrollTime ?: "10:30 PM"}
                        
                        Task:
                        Provide 3 ultra-specific, high-dopamine alternative feed strategies and creator channels tailored for a ${currentProfile.age} year old ${currentProfile.gender}.
                        Explain how replacing short-form doomscrolling with these long-form or constructive topics reverses attention fragmentation and elevates dopamine baseline.
                        Keep it inspiring, concise, and structured with bullet points.
                    """.trimIndent()

                    val result = callGeminiRest(apiKey, prompt)
                    _aiRecommendationAdvice.value = result ?: buildLocalAiAdvice(currentProfile, report)
                } catch (e: Exception) {
                    _aiRecommendationAdvice.value = buildLocalAiAdvice(currentProfile, report)
                } finally {
                    _isLoadingAi.value = false
                }
            }
        }
    }

    private suspend fun callGeminiRest(apiKey: String, prompt: String): String? = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val jsonPayload = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: return@withContext null
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text")
                }
            }
        }
        null
    }

    private fun buildLocalAiAdvice(profile: UserProfile, report: WeeklyScreenTimeReport?): String {
        val totalR = report?.totalReelsScrolled ?: 320
        val topApp = report?.topPlatform ?: "Instagram Reels"
        val demographic = "${profile.age}-year-old ${profile.gender.lowercase()}"

        return """
            🎯 Personalized Feed Architecture for $demographic:
            
            1. Dopamine Reset & Substitution Strategy:
               • You scrolled $totalR reels this week (predominantly on $topApp). Short-form videos deliver intermittent micro-dopamine spikes every 12 seconds, causing prefrontal cortex fatigue.
               • Swap the 10:30 PM bedtime scroll with 20 minutes of high-curiosity long-form content (e.g. Veritasium, Huberman Lab, or Kurzgesagt).
               
            2. Curated Constructive Feeds Tailored for You:
               ${if (profile.gender == "Male") {
            "• Physical Strength & Discipline: Renaissance Periodization / Jeff Nippard\n• Deep Tech & Systems: Fireship / Lex Fridman\n• Stoic Resilience: The Daily Stoic"
        } else if (profile.gender == "Female") {
            "• Mobility & Nervous System Reset: Move With Nicole / Natacha Océane\n• Financial Autonomy: The Financial Diet\n• Creative Science: Kurzgesagt"
        } else {
            "• Intellectual Exploration: Veritasium & 3Blue1Brown\n• Deep Focus: Ali Abdaal / Cal Newport\n• Natural Wonder: BBC Earth"
        }}
            
            3. Algorithmic Detox Prescription:
               • Enforce your daily limit of ${profile.dailyReelLimit} reels.
               • Use the 'Open Constructive Feed' links below when the scrolling urge strikes.
        """.trimIndent()
    }
}
