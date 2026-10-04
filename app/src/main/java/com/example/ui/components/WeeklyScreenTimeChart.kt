package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPlatformData
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.InstagramColor
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TikTokColor
import com.example.ui.theme.YouTubeColor

enum class ChartMetric {
    SCREEN_TIME,
    REEL_COUNT
}

@Composable
fun WeeklyScreenTimeChart(
    dailyData: List<DailyPlatformData>,
    dailyReelGoal: Int,
    dailyScreenTimeGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.SCREEN_TIME) }
    var selectedIndex by remember { mutableIntStateOf(dailyData.indexOfLast { it.isToday }.coerceAtLeast(dailyData.size - 1)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with title and Metric switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Activity Habit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Interactive breakdown by day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Switch chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedMetric == ChartMetric.SCREEN_TIME,
                        onClick = { selectedMetric = ChartMetric.SCREEN_TIME },
                        label = { Text("Time", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_time")
                    )
                    FilterChip(
                        selected = selectedMetric == ChartMetric.REEL_COUNT,
                        onClick = { selectedMetric = ChartMetric.REEL_COUNT },
                        label = { Text("Reels", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_chip_reels")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Chart Canvas
            val maxValue = remember(dailyData, selectedMetric) {
                if (selectedMetric == ChartMetric.SCREEN_TIME) {
                    val maxVal = dailyData.maxOfOrNull { it.totalMinutes } ?: 180L
                    (maxVal.coerceAtLeast(dailyScreenTimeGoalMinutes.toLong()) * 1.25f).coerceAtLeast(60f)
                } else {
                    val maxVal = dailyData.maxOfOrNull { it.totalReels } ?: 50
                    (maxVal.coerceAtLeast(dailyReelGoal) * 1.25f).coerceAtLeast(20f)
                }
            }

            val goalValue = if (selectedMetric == ChartMetric.SCREEN_TIME) {
                dailyScreenTimeGoalMinutes.toFloat()
            } else {
                dailyReelGoal.toFloat()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(dailyData) {
                            detectTapGestures { offset ->
                                val barWidthTotal = size.width / dailyData.size
                                val index = (offset.x / barWidthTotal).toInt().coerceIn(0, dailyData.size - 1)
                                selectedIndex = index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height - 30.dp.toPx() // leave room for labels
                    val count = dailyData.size
                    val barWidth = (width / count) * 0.48f
                    val slotWidth = width / count

                    // Draw Goal Line
                    val goalY = height - (goalValue / maxValue) * height
                    if (goalY in 0f..height) {
                        drawLine(
                            color = AccentCyan.copy(alpha = 0.65f),
                            start = Offset(0f, goalY),
                            end = Offset(width, goalY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    }

                    dailyData.forEachIndexed { index, data ->
                        val value = if (selectedMetric == ChartMetric.SCREEN_TIME) {
                            data.totalMinutes.toFloat()
                        } else {
                            data.totalReels.toFloat()
                        }

                        val barHeight = ((value / maxValue) * height).coerceAtLeast(6.dp.toPx())
                        val x = (index * slotWidth) + (slotWidth - barWidth) / 2f
                        val y = height - barHeight

                        val isSelected = (index == selectedIndex)
                        val isOverGoal = value > goalValue

                        // Segmented or Gradient Fill
                        val brush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(AccentCyan, PrimaryIndigo)
                            )
                        } else if (isOverGoal) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF43F5E), Color(0xFFBE123C))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(PrimaryIndigo.copy(alpha = 0.85f), PrimaryIndigo.copy(alpha = 0.45f))
                            )
                        }

                        // Bar background slot
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.04f),
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Main Bar
                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Top indicator cap for selected item
                        if (isSelected) {
                            drawCircle(
                                color = Color.White,
                                radius = 4.dp.toPx(),
                                center = Offset(x + barWidth / 2f, y - 6.dp.toPx())
                            )
                        }
                    }
                }
            }

            // Day Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dailyData.forEachIndexed { index, item ->
                    val isSelected = (index == selectedIndex)
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { selectedIndex = index }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Day Detail Tooltip Card
            if (selectedIndex in dailyData.indices) {
                val day = dailyData[selectedIndex]
                DayDetailCard(day = day, goalValue = goalValue.toInt(), metric = selectedMetric)
            }
        }
    }
}

@Composable
private fun DayDetailCard(
    day: DailyPlatformData,
    goalValue: Int,
    metric: ChartMetric
) {
    val isOverLimit = if (metric == ChartMetric.SCREEN_TIME) {
        day.totalMinutes > goalValue
    } else {
        day.totalReels > goalValue
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = if (isOverLimit) Color(0xFFF43F5E).copy(alpha = 0.5f) else PrimaryIndigo.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (day.isToday) SuccessEmerald else PrimaryIndigo,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (day.isToday) "${day.dayLabel} (Today)" else "${day.dayLabel} (${day.dateString})",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = if (isOverLimit) "⚠️ Over Goal" else "✅ Goal Protected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverLimit) Color(0xFFF43F5E) else SuccessEmerald
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Screen Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val hrs = day.totalMinutes / 60
                    val mins = day.totalMinutes % 60
                    val timeStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                    Text(
                        text = timeStr,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "Reels / Shorts Scrolled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${day.totalReels} reels",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Platform mini breakdown pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformMiniPill(
                    name = "Instagram",
                    mins = day.instagramMinutes,
                    color = InstagramColor,
                    modifier = Modifier.weight(1f)
                )
                PlatformMiniPill(
                    name = "YouTube",
                    mins = day.youtubeMinutes,
                    color = YouTubeColor,
                    modifier = Modifier.weight(1f)
                )
                PlatformMiniPill(
                    name = "TikTok",
                    mins = day.tiktokMinutes,
                    color = TikTokColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PlatformMiniPill(
    name: String,
    mins: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = name,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${mins}m",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
