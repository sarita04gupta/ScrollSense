package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.ScrollSenseApplication
import com.example.data.model.PlatformType
import com.example.data.model.ReelScrollEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReelScrollAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastScrollTimestamp: Long = 0L
    private val scrollDebounceMs = 900L // debounce consecutive swipe frames to single reel
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "ScrollSense Accessibility Service Connected")
        ScrollTrackerState.setServiceConnected(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return
        val platform = PlatformType.fromPackageName(pkgName)

        // Only track targeted social apps
        if (platform == PlatformType.OTHER && !isTargetSocialApp(pkgName)) {
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                handleScrollEvent(pkgName, platform, event)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // User opened/switched to target app
                ScrollTrackerState.onReelScrolled(platform.displayName, ScrollTrackerState.liveReelsToday.value)
            }
        }
    }

    private fun handleScrollEvent(pkgName: String, platform: PlatformType, event: AccessibilityEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScrollTimestamp < scrollDebounceMs) {
            return
        }

        // Verify if it's likely a short video / reel container
        val isReelContext = isReelOrShortsContext(pkgName, event)
        if (!isReelContext) {
            return
        }

        lastScrollTimestamp = currentTime

        val todayDate = dateFormat.format(Date(currentTime))
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        serviceScope.launch {
            try {
                val app = applicationContext as? ScrollSenseApplication ?: return@launch
                val dao = app.database.scrollSenseDao()

                // Insert scroll event
                dao.insertScrollEvent(
                    ReelScrollEntity(
                        platform = platform.displayName,
                        packageName = pkgName,
                        timestamp = currentTime,
                        dateString = todayDate,
                        hourOfDay = hour,
                        dayOfWeek = dayOfWeek,
                        scrollDelta = 1,
                        durationSeconds = 12
                    )
                )

                // Update today's aggregate
                app.repository.incrementScroll(platform.displayName, todayDate, hour)

                val newCount = ScrollTrackerState.liveReelsToday.value + 1
                ScrollTrackerState.onReelScrolled(platform.displayName, newCount)

                // Check for mindful nudge
                val profile = app.repository.getUserProfile()
                if (profile.nudgeNotificationEnabled) {
                    if (newCount == profile.dailyReelLimit ||
                        (newCount > profile.dailyReelLimit && newCount % 25 == 0)
                    ) {
                        NotificationHelper.sendReelNudge(
                            applicationContext,
                            newCount,
                            platform.displayName
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recording reel scroll", e)
            }
        }
    }

    private fun isReelOrShortsContext(pkgName: String, event: AccessibilityEvent): Boolean {
        // TikTok is 100% short videos
        if (pkgName.contains("zhiliaoapp") || pkgName.contains("trill") || pkgName.contains("tiktok")) {
            return true
        }

        // Check node hierarchy for YouTube Shorts and Instagram Reels identifiers
        try {
            val rootNode = rootInActiveWindow ?: return true
            return searchForReelKeywords(rootNode, pkgName)
        } catch (e: Exception) {
            // Fallback to true if accessibility node tree inspection is restricted
            return true
        }
    }

    private fun searchForReelKeywords(node: AccessibilityNodeInfo?, pkg: String): Boolean {
        if (node == null) return false

        val viewId = node.viewIdResourceName?.lowercase(Locale.ROOT) ?: ""
        val text = node.text?.toString()?.lowercase(Locale.ROOT) ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase(Locale.ROOT) ?: ""
        val className = node.className?.toString()?.lowercase(Locale.ROOT) ?: ""

        val isReelSignature = viewId.contains("reel") || viewId.contains("clips") ||
                viewId.contains("short") || viewId.contains("player") ||
                text.contains("reels") || text.contains("shorts") || text.contains("remix") ||
                contentDesc.contains("reels") || contentDesc.contains("shorts") ||
                className.contains("viewpager") || className.contains("recycler")

        if (isReelSignature) return true

        // Shallow inspect children (limit depth to 2 to keep performance blazing fast)
        val childCount = node.childCount.coerceAtMost(6)
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val childViewId = child.viewIdResourceName?.lowercase(Locale.ROOT) ?: ""
            val childText = child.text?.toString()?.lowercase(Locale.ROOT) ?: ""
            if (childViewId.contains("reel") || childViewId.contains("clips") ||
                childViewId.contains("short") || childText.contains("reels") || childText.contains("shorts")
            ) {
                child.recycle()
                return true
            }
            child.recycle()
        }

        return false
    }

    private fun isTargetSocialApp(pkg: String): Boolean {
        return pkg.contains("instagram") ||
                pkg.contains("youtube") ||
                pkg.contains("tiktok") ||
                pkg.contains("facebook") ||
                pkg.contains("snapchat")
    }

    override fun onInterrupt() {
        Log.d(TAG, "ScrollSense Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        ScrollTrackerState.setServiceConnected(false)
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "ReelScrollService"
    }
}
