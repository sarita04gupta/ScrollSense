package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class PlatformType(
    val displayName: String,
    val packageName: String,
    val defaultColor: Color,
    val iconName: String
) {
    INSTAGRAM(
        displayName = "Instagram Reels",
        packageName = "com.instagram.android",
        defaultColor = Color(0xFFE1306C),
        iconName = "instagram"
    ),
    YOUTUBE(
        displayName = "YouTube Shorts",
        packageName = "com.google.android.youtube",
        defaultColor = Color(0xFFFF0000),
        iconName = "youtube"
    ),
    TIKTOK(
        displayName = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        defaultColor = Color(0xFF00F2FE),
        iconName = "tiktok"
    ),
    FACEBOOK(
        displayName = "Facebook Reels",
        packageName = "com.facebook.katana",
        defaultColor = Color(0xFF1877F2),
        iconName = "facebook"
    ),
    SNAPCHAT(
        displayName = "Snapchat Spotlight",
        packageName = "com.snapchat.android",
        defaultColor = Color(0xFFFFFC00),
        iconName = "snapchat"
    ),
    OTHER(
        displayName = "Other Media",
        packageName = "other",
        defaultColor = Color(0xFF8B5CF6),
        iconName = "other"
    );

    companion object {
        fun fromPackageName(pkg: String): PlatformType {
            return when {
                pkg.contains("instagram") -> INSTAGRAM
                pkg.contains("youtube") -> YOUTUBE
                pkg.contains("tiktok") || pkg.contains("musically") || pkg.contains("trill") -> TIKTOK
                pkg.contains("facebook") -> FACEBOOK
                pkg.contains("snapchat") -> SNAPCHAT
                else -> OTHER
            }
        }
    }
}
