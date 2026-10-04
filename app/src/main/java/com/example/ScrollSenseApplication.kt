package com.example

import android.app.Application
import com.example.data.local.ScrollSenseDatabase
import com.example.data.repository.ScrollRepository
import com.example.service.NotificationHelper
import com.example.service.ScrollTrackerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScrollSenseApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { ScrollSenseDatabase.getInstance(this) }
    val repository by lazy { ScrollRepository(this, database.scrollSenseDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        applicationScope.launch {
            repository.initializeDatabase()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val summary = database.scrollSenseDao().getDailySummary(todayStr)
            val todayCount = summary?.totalReels ?: 0
            ScrollTrackerState.updateReelCountToday(todayCount)
        }
    }
}
