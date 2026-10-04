package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyScrollSummaryEntity
import com.example.data.model.ReelScrollEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrollSenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScrollEvent(event: ReelScrollEntity): Long

    @Query("SELECT * FROM reel_scroll_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentScrollEvents(): Flow<List<ReelScrollEntity>>

    @Query("SELECT * FROM reel_scroll_events WHERE dateString = :dateString")
    fun getEventsForDate(dateString: String): Flow<List<ReelScrollEntity>>

    @Query("SELECT COUNT(*) FROM reel_scroll_events WHERE dateString = :dateString")
    fun getReelCountForDate(dateString: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM reel_scroll_events WHERE dateString = :dateString AND platform = :platform")
    suspend fun getReelCountForPlatformOnDate(dateString: String, platform: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailyScrollSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummaries(summaries: List<DailyScrollSummaryEntity>)

    @Query("SELECT * FROM daily_scroll_summaries ORDER BY dateString ASC")
    fun getAllDailySummaries(): Flow<List<DailyScrollSummaryEntity>>

    @Query("SELECT * FROM daily_scroll_summaries WHERE dateString = :dateString LIMIT 1")
    suspend fun getDailySummary(dateString: String): DailyScrollSummaryEntity?

    @Query("SELECT * FROM daily_scroll_summaries ORDER BY dateString DESC LIMIT 7")
    fun getRecentSevenDays(): Flow<List<DailyScrollSummaryEntity>>

    @Query("SELECT COUNT(*) FROM reel_scroll_events")
    suspend fun getTotalScrollEventsCount(): Int
}
