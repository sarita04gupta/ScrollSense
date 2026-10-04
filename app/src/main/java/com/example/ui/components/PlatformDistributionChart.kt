package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlatformBreakdown
import com.example.ui.theme.FacebookColor
import com.example.ui.theme.InstagramColor
import com.example.ui.theme.OtherPlatformColor
import com.example.ui.theme.SnapchatColor
import com.example.ui.theme.TikTokColor
import com.example.ui.theme.YouTubeColor
import kotlin.math.atan2

@Composable
fun PlatformDistributionChart(
    platformTotals: Map<String, PlatformBreakdown>,
    modifier: Modifier = Modifier
) {
    val items = remember(platformTotals) { platformTotals.values.toList() }
    var selectedPlatform by remember(platformTotals) {
        mutableStateOf(items.maxByOrNull { it.screenTimeMinutes }?.platformName ?: "Instagram Reels")
    }

    val selectedBreakdown = remember(selectedPlatform, platformTotals) {
        platformTotals[selectedPlatform] ?: items.firstOrNull()
    }

    fun getPlatformColor(name: String): Color {
        return when {
            name.contains("Instagram") -> InstagramColor
            name.contains("YouTube") -> YouTubeColor
            name.contains("TikTok") -> TikTokColor
            name.contains("Facebook") -> FacebookColor
            name.contains("Snapchat") -> SnapchatColor
            else -> OtherPlatformColor
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("platform_distribution_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Platform Scrolling Share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap any segment to inspect platform details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive Donut Canvas
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(170.dp)
                            .pointerInput(items) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val angleRad = atan2(offset.y - center.y, offset.x - center.x)
                                    var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                                    if (angleDeg < 0) angleDeg += 360f

                                    // Map angle to arc
                                    var currentAngle = 270f
                                    for (item in items) {
                                        val sweep = item.percentage * 360f
                                        val endAngle = (currentAngle + sweep) % 360f
                                        val inRange = if (currentAngle + sweep > 360f) {
                                            angleDeg >= currentAngle || angleDeg <= endAngle
                                        } else {
                                            angleDeg in currentAngle..endAngle
                                        }
                                        if (inRange) {
                                            selectedPlatform = item.platformName
                                            break
                                        }
                                        currentAngle = (currentAngle + sweep) % 360f
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 24.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        var startAngle = 270f
                        items.forEach { item ->
                            val isSelected = item.platformName == selectedPlatform
                            val sweepAngle = (item.percentage * 360f).coerceAtLeast(3f)
                            val color = getPlatformColor(item.platformName)

                            drawArc(
                                color = if (isSelected) color else color.copy(alpha = 0.55f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 3f, // gap
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = if (isSelected) strokeWidth * 1.15f else strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center label of Donut
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${((selectedBreakdown?.percentage ?: 0f) * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "of feeds",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend and Details Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEach { item ->
                        val isSelected = item.platformName == selectedPlatform
                        val color = getPlatformColor(item.platformName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPlatform = item.platformName }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.platformName.replace(" Reels", "").replace(" Shorts", ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${item.reelCount} reels",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Platform Banner
            selectedBreakdown?.let { detail ->
                val platformColor = getPlatformColor(detail.platformName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(platformColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = detail.platformName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Estimated screen time: ${detail.screenTimeMinutes / 60}h ${detail.screenTimeMinutes % 60}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "avg ${(detail.reelCount / 7)} reels/day",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = platformColor
                    )
                }
            }
        }
    }
}
