/*
 * Pixel Music (2026)
 * © zlgskt6 — github.com/zlgskt6
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.zlgskt6.pixelmusic.ui.component

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zlgskt6.pixelmusic.R
import kotlin.math.roundToInt

/**
 * Apple Music Sing button toggle.
 */
@Composable
fun AppleMusicSingButton(
    isSingActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    val view = LocalView.current
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSingActive) 1f else 0.75f,
        animationSpec = tween(250),
        label = "singBtnAlpha"
    )

    IconButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = modifier.size(36.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    if (isSingActive) {
                        tint.copy(alpha = 0.28f)
                    } else {
                        Color.Transparent
                    }
                )
                .border(
                    width = if (isSingActive) 1.5.dp else 0.dp,
                    color = if (isSingActive) tint.copy(alpha = 0.6f) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.mic),
                contentDescription = "Apple Music Sing (Vocal Control)",
                tint = if (isSingActive) tint else tint.copy(alpha = animatedAlpha),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Apple Music Sing vertical vocal fader capsule.
 */
@Composable
fun AppleMusicSingFader(
    vocalVolume: Float,
    isSingActive: Boolean,
    onVocalVolumeChange: (Float) -> Unit,
    onToggleSing: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White,
) {
    val view = LocalView.current
    var isDragging by remember { mutableStateOf(false) }

    val percentage = (vocalVolume * 100f).roundToInt().coerceIn(0, 100)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    // Tap background to dismiss
                    onDismiss()
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .pointerInput(Unit) {
                    // Catch taps on the capsule itself so it doesn't dismiss
                    detectTapGestures { }
                }
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color(0xFF1E1E24).copy(alpha = 0.85f),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(vertical = 14.dp, horizontal = 10.dp)
                .width(60.dp),
        ) {
            // Label / Percentage indicator
            Text(
                text = if (!isSingActive) "100%" else "$percentage%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                ),
                color = accentColor.copy(alpha = 0.9f),
            )

            Spacer(Modifier.height(10.dp))

            // Vertical Slider Capsule
            BoxWithConstraints(
                modifier = Modifier
                    .width(40.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val newVolume = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                                onVocalVolumeChange(newVolume)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            },
                            onDragEnd = {
                                isDragging = false
                            },
                            onDragCancel = {
                                isDragging = false
                            },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                val newVolume = (1f - (change.position.y / size.height.toFloat())).coerceIn(0f, 1f)
                                onVocalVolumeChange(newVolume)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newVolume = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                            onVocalVolumeChange(newVolume)
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                val totalHeight = maxHeight
                val fillFraction = if (isSingActive) vocalVolume.coerceIn(0f, 1f) else 1f

                // Level fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fillFraction)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.95f),
                                    accentColor.copy(alpha = 0.75f),
                                )
                            )
                        )
                )

                // Microphone icon indicator inside slider
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.mic),
                        contentDescription = null,
                        tint = if (fillFraction > 0.35f) Color.Black.copy(alpha = 0.8f) else accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bottom quick toggle button: Sing on/off
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSingActive) accentColor else Color.White.copy(alpha = 0.15f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 17.dp),
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onToggleSing()
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.mic),
                    contentDescription = "Toggle Sing Mode",
                    tint = if (isSingActive) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
