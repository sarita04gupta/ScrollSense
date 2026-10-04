package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ScrollTrackerState {
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _liveReelsToday = MutableStateFlow(0)
    val liveReelsToday: StateFlow<Int> = _liveReelsToday.asStateFlow()

    private val _currentActivePlatform = MutableStateFlow<String?>(null)
    val currentActivePlatform: StateFlow<String?> = _currentActivePlatform.asStateFlow()

    private val _lastScrollTime = MutableStateFlow(0L)
    val lastScrollTime: StateFlow<Long> = _lastScrollTime.asStateFlow()

    private val _recentSpreeCount = MutableStateFlow(0)
    val recentSpreeCount: StateFlow<Int> = _recentSpreeCount.asStateFlow()

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    fun onReelScrolled(platform: String, countToday: Int) {
        _liveReelsToday.value = countToday
        _currentActivePlatform.value = platform
        _lastScrollTime.value = System.currentTimeMillis()
        _recentSpreeCount.value += 1
    }

    fun updateReelCountToday(count: Int) {
        _liveReelsToday.value = count
    }

    fun resetSpree() {
        _recentSpreeCount.value = 0
    }
}
