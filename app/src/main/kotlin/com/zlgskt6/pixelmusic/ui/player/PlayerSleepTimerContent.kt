package com.zlgskt6.pixelmusic.ui.player

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zlgskt6.pixelmusic.R
import com.zlgskt6.pixelmusic.constants.EnableHapticFeedbackKey
import com.zlgskt6.pixelmusic.playback.PlayerConnection
import com.zlgskt6.pixelmusic.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerSleepTimerContent(
    playerConnection: PlayerConnection,
    textBackgroundColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onShowLyrics: (() -> Unit)? = null,
    onShowQueue: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val (enableHapticFeedback) = rememberPreference(EnableHapticFeedbackKey, true)
    val sleepTimer = playerConnection.service.sleepTimer

    fun triggerClickHaptic() {
        if (enableHapticFeedback) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CONTEXT_CLICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
            )
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun triggerSliderTickHaptic() {
        if (enableHapticFeedback) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
            )
        }
    }

    var timeRemainingMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(sleepTimer.triggerTime, sleepTimer.pauseWhenSongEnd, sleepTimer.isActive) {
        if (sleepTimer.isActive) {
            while (isActive) {
                timeRemainingMs = if (sleepTimer.pauseWhenSongEnd) {
                    (playerConnection.player.duration - playerConnection.player.currentPosition).coerceAtLeast(0L)
                } else {
                    (sleepTimer.triggerTime - System.currentTimeMillis()).coerceAtLeast(0L)
                }
                delay(1000L)
            }
        }
    }

    var sliderMinutes by remember { mutableFloatStateOf(30f) }
    var isEndOfSongSelected by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Top Navigation / Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(textBackgroundColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.bedtime),
                            contentDescription = null,
                            tint = textBackgroundColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.sleep_timer),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textBackgroundColor,
                        )
                        Text(
                            text = if (sleepTimer.isActive) "Active timer" else "Fade playback off",
                            style = MaterialTheme.typography.labelSmall,
                            color = textBackgroundColor.copy(alpha = 0.7f),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (onShowLyrics != null) {
                        IconButton(
                            onClick = {
                                triggerClickHaptic()
                                onShowLyrics()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(textBackgroundColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.lyrics),
                                    contentDescription = stringResource(R.string.lyrics),
                                    tint = textBackgroundColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    if (onShowQueue != null) {
                        IconButton(
                            onClick = {
                                triggerClickHaptic()
                                onShowQueue()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(textBackgroundColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.queue_music),
                                    contentDescription = stringResource(R.string.queue),
                                    tint = textBackgroundColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    // Close button (fades cover art back in)
                    IconButton(
                        onClick = {
                            triggerClickHaptic()
                            onDismiss()
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(textBackgroundColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = "Close",
                                tint = textBackgroundColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Timer Card Body
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = textBackgroundColor.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = textBackgroundColor.copy(alpha = 0.14f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (sleepTimer.isActive) {
                        // ACTIVE TIMER STATE
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .scale(if (sleepTimer.isActive) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            textBackgroundColor.copy(alpha = 0.25f),
                                            textBackgroundColor.copy(alpha = 0.08f),
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.bedtime),
                                contentDescription = null,
                                tint = textBackgroundColor,
                                modifier = Modifier.size(36.dp),
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = if (sleepTimer.pauseWhenSongEnd) {
                                stringResource(R.string.end_of_song)
                            } else {
                                formatRemainingTime(timeRemainingMs)
                            },
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = if (sleepTimer.pauseWhenSongEnd) 26.sp else 38.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = textBackgroundColor,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = if (sleepTimer.pauseWhenSongEnd) {
                                "Music will pause when the track finishes"
                            } else {
                                "Time remaining before playback pauses"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textBackgroundColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        Spacer(Modifier.height(18.dp))

                        // Extend timer buttons (if not end of song)
                        if (!sleepTimer.pauseWhenSongEnd) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                listOf(5, 15, 30).forEach { extraMin ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = textBackgroundColor.copy(alpha = 0.12f),
                                        modifier = Modifier.clickable {
                                            triggerClickHaptic()
                                            val currentLeftMinutes = (timeRemainingMs / 60000L).toInt()
                                            sleepTimer.start((currentLeftMinutes + extraMin).coerceAtLeast(extraMin))
                                        },
                                    ) {
                                        Text(
                                            text = "+$extraMin min",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textBackgroundColor,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))
                        }

                        // Turn Off Timer Button
                        FilledTonalButton(
                            onClick = {
                                triggerClickHaptic()
                                sleepTimer.clear()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = textBackgroundColor.copy(alpha = 0.16f),
                                contentColor = textBackgroundColor,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Turn Off Timer",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        // IDLE / SET TIMER STATE
                        Text(
                            text = if (isEndOfSongSelected) {
                                stringResource(R.string.end_of_song)
                            } else {
                                pluralStringResource(
                                    R.plurals.minute,
                                    sliderMinutes.roundToInt(),
                                    sliderMinutes.roundToInt(),
                                )
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = textBackgroundColor,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = if (isEndOfSongSelected) {
                                "Stops playback when the current track ends"
                            } else {
                                "Set sleep timer duration"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textBackgroundColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp),
                        )

                        Spacer(Modifier.height(16.dp))

                        // Quick Preset Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            listOf(15, 30, 45, 60, 90).forEach { minutes ->
                                val isSelected = !isEndOfSongSelected && sliderMinutes.roundToInt() == minutes
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) textBackgroundColor else textBackgroundColor.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            triggerClickHaptic()
                                            isEndOfSongSelected = false
                                            sliderMinutes = minutes.toFloat()
                                        },
                                ) {
                                    Text(
                                        text = "${minutes}m",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) iconButtonColor else textBackgroundColor,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    )
                                }
                            }

                            // End of song preset chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isEndOfSongSelected) textBackgroundColor else textBackgroundColor.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        triggerClickHaptic()
                                        isEndOfSongSelected = true
                                    },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.skip_next),
                                        contentDescription = null,
                                        tint = if (isEndOfSongSelected) iconButtonColor else textBackgroundColor,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.end_of_song),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isEndOfSongSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isEndOfSongSelected) iconButtonColor else textBackgroundColor,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Slider (when minutes mode is active)
                        AnimatedVisibility(
                            visible = !isEndOfSongSelected,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(200)),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Slider(
                                    value = sliderMinutes,
                                    onValueChange = { newValue ->
                                        isEndOfSongSelected = false
                                        val oldStep = (sliderMinutes / 5f).roundToInt()
                                        val newStep = (newValue / 5f).roundToInt()
                                        if (newStep != oldStep) {
                                            triggerSliderTickHaptic()
                                        }
                                        sliderMinutes = newValue
                                    },
                                    valueRange = 5f..120f,
                                    steps = (120 - 5) / 5 - 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = textBackgroundColor,
                                        activeTrackColor = textBackgroundColor,
                                        inactiveTrackColor = textBackgroundColor.copy(alpha = 0.2f),
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "5 min",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textBackgroundColor.copy(alpha = 0.5f),
                                    )
                                    Text(
                                        text = "2 hours",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textBackgroundColor.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Start Timer Button
                        Button(
                            onClick = {
                                triggerClickHaptic()
                                if (isEndOfSongSelected) {
                                    sleepTimer.start(-1)
                                } else {
                                    sleepTimer.start(sliderMinutes.roundToInt())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = textBackgroundColor,
                                contentColor = iconButtonColor,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.bedtime),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isEndOfSongSelected) "Set End of Song Timer" else "Start Timer",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRemainingTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

