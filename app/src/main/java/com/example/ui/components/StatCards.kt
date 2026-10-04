package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlatformType
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentRose
import com.example.ui.theme.InstagramColor
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.TikTokColor
import com.example.ui.theme.YouTubeColor

@Composable
fun LiveReelsHeroCard(
    reelsToday: Int,
    dailyLimit: Int,
    screenTimeMinutesToday: Long,
    isServiceActive: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (reelsToday.toFloat() / dailyLimit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(600), label = "reel_progress")
    val isLimitExceeded = reelsToday >= dailyLimit

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_reels_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Service Status Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (isServiceActive) SuccessEmerald.copy(alpha = 0.15f) else AccentAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isServiceActive) SuccessEmerald else AccentAmber,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isServiceActive) "Background Monitor Active" else "Service Setup Needed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isServiceActive) SuccessEmerald else AccentAmber
                    )
                }

                if (!isServiceActive) {
                    OutlinedButton(
                        onClick = onOpenAccessibilitySettings,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("enable_accessibility_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enable", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Counter Ring and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Progress Dial
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(130.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        strokeWidth = 10.dp,
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(130.dp),
                        color = if (isLimitExceeded) AccentRose else AccentCyan,
                        strokeWidth = 10.dp,
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$reelsToday",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/ $dailyLimit limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats breakdown on the right
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroStatItem(
                        icon = Icons.Default.Timer,
                        label = "Screen Time Today",
                        value = "${screenTimeMinutesToday / 60}h ${screenTimeMinutesToday % 60}m",
                        accentColor = PrimaryIndigo
                    )

                    HeroStatItem(
                        icon = Icons.Default.Psychology,
                        label = "Dopamine Stress",
                        value = if (reelsToday < 20) "Calm & Stable" else if (reelsToday < 50) "Moderate" else "High Fatigue",
                        accentColor = if (reelsToday < 20) SuccessEmerald else if (reelsToday < 50) AccentAmber else AccentRose
                    )

                    HeroStatItem(
                        icon = if (isLimitExceeded) Icons.Default.Warning else Icons.Default.CheckCircle,
                        label = "Detox Budget",
                        value = if (isLimitExceeded) "${reelsToday - dailyLimit} over goal" else "${dailyLimit - reelsToday} left",
                        accentColor = if (isLimitExceeded) AccentRose else SuccessEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InteractiveDemoSimulatorCard(
    onSimulateScroll: (PlatformType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simulator_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Simulator",
                        tint = AccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Reel Scroller Simulator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Instant Testing",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Simulate a live reel or short swipe to test real-time counter updates, room logging, and chart trends:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSimulateScroll(PlatformType.INSTAGRAM) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_instagram_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = InstagramColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+1 IG Reel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSimulateScroll(PlatformType.YOUTUBE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_youtube_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+1 YT Short", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSimulateScroll(PlatformType.TIKTOK) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_tiktok_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+1 TikTok", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
