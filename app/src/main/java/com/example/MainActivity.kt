package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ScrollSenseViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedSuggestionsScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.WeeklyReportScreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.ScrollSenseTheme

enum class Screen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Filled.Home, Icons.Outlined.Home, "nav_dashboard"),
    WEEKLY_REPORT("Weekly Report", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_weekly_report"),
    SMART_FEEDS("Best Feeds", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_smart_feeds"),
    SETTINGS("Settings", Icons.Filled.Person, Icons.Outlined.Person, "nav_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ScrollSenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScrollSenseTheme {
                var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

                val liveReelsToday by viewModel.liveReelsToday.collectAsStateWithLifecycle()
                val isServiceConnected by viewModel.isServiceConnected.collectAsStateWithLifecycle()
                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val weeklyReport by viewModel.weeklyReport.collectAsStateWithLifecycle()
                val recommendedFeeds by viewModel.recommendedFeeds.collectAsStateWithLifecycle()
                val detoxPlanSummary by viewModel.detoxPlanSummary.collectAsStateWithLifecycle()
                val hasUsageStatsPermission by viewModel.hasUsageStatsPermission.collectAsStateWithLifecycle()
                val aiRecommendationAdvice by viewModel.aiRecommendationAdvice.collectAsStateWithLifecycle()
                val isLoadingAi by viewModel.isLoadingAi.collectAsStateWithLifecycle()

                // Refresh permissions on resume
                LaunchedEffect(Unit) {
                    viewModel.refreshPermissions()
                    viewModel.refreshWeeklyReport()
                }

                // Handle Back button to return to Dashboard if on sub-screens
                if (currentScreen != Screen.DASHBOARD) {
                    BackHandler {
                        currentScreen = Screen.DASHBOARD
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Screen.values().forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    modifier = Modifier.testTag(screen.testTag),
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryIndigo,
                                        selectedTextColor = PrimaryIndigo,
                                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        Screen.DASHBOARD -> DashboardScreen(
                            liveReelsToday = liveReelsToday,
                            isServiceActive = isServiceConnected,
                            userProfile = userProfile,
                            weeklyReport = weeklyReport,
                            recommendedFeeds = recommendedFeeds,
                            onSimulateScroll = { platform -> viewModel.simulateReelScroll(platform) },
                            onNavigateToReport = { currentScreen = Screen.WEEKLY_REPORT },
                            onNavigateToFeeds = { currentScreen = Screen.SMART_FEEDS },
                            onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.WEEKLY_REPORT -> WeeklyReportScreen(
                            weeklyReport = weeklyReport,
                            userProfile = userProfile,
                            hasUsageStatsPermission = hasUsageStatsPermission,
                            onRefresh = { viewModel.refreshWeeklyReport() },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.SMART_FEEDS -> FeedSuggestionsScreen(
                            userProfile = userProfile,
                            recommendedFeeds = recommendedFeeds,
                            detoxPlanSummary = detoxPlanSummary,
                            aiRecommendationAdvice = aiRecommendationAdvice,
                            isLoadingAi = isLoadingAi,
                            onRequestAiAdvice = { viewModel.requestAiPersonalizedAnalysis() },
                            onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.SETTINGS -> ProfileSettingsScreen(
                            userProfile = userProfile,
                            isServiceActive = isServiceConnected,
                            hasUsageStatsPermission = hasUsageStatsPermission,
                            onSaveProfile = { newProfile -> viewModel.updateUserProfile(newProfile) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
        viewModel.refreshWeeklyReport()
    }
}
