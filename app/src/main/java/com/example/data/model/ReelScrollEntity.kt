package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reel_scroll_events")
data class ReelScrollEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platform: String,
    val packageName: String,
    val timestamp: Long,
    val dateString: String,      // Format: "yyyy-MM-dd"
    val hourOfDay: Int,          // 0 - 23
    val dayOfWeek: Int,          // Calendar.SUNDAY ... Calendar.SATURDAY
    val scrollDelta: Int = 1,
    val durationSeconds: Long = 15 // estimated or measured time viewing reel
)

@Entity(tableName = "daily_scroll_summaries")
data class DailyScrollSummaryEntity(
    @PrimaryKey
    val dateString: String,      // Format: "yyyy-MM-dd"
    val dayLabel: String,        // "Mon", "Tue", etc.
    val totalReels: Int,
    val instagramReels: Int,
    val youtubeShorts: Int,
    val tiktokReels: Int,
    val otherReels: Int,
    val totalScreenTimeMinutes: Long,
    val socialScreenTimeMinutes: Long,
    val peakHour: Int = 21,
    val goalMet: Boolean = true
)
