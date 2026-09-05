package com.shahdullah.nomatune.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.shahdullah.nomatune.LocalDatabase
import com.shahdullah.nomatune.LocalPlayerConnection
import com.shahdullah.nomatune.R
import com.shahdullah.nomatune.constants.LyricsMode
import com.shahdullah.nomatune.constants.LyricsModeKey
import com.shahdullah.nomatune.constants.ThumbnailCornerRadiusKey
import com.shahdullah.nomatune.di.LyricsHelperEntryPoint
import com.shahdullah.nomatune.models.MediaMetadata
import com.shahdullah.nomatune.ui.component.LocalMenuState
import com.shahdullah.nomatune.ui.menu.LyricsMenu
import com.shahdullah.nomatune.utils.rememberEnumPreference
import com.shahdullah.nomatune.utils.rememberPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Integrated player view that smoothly transitions between the full-size centered cover art
 * and an inline lyrics display where the cover art shrinks and docks in the top-left corner.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerArtworkLyricsArea(
    mediaMetadata: MediaMetadata,
    sliderPositionProvider: () -> Long?,
    isPlayerExpanded: Boolean,
    isLyricsVisible: Boolean,
    onDismissLyrics: () -> Unit,
    lyricsSyncOffset: Int,
    onLyricsSyncOffsetChange: (Int) -> Unit,
    nestedScrollConnection: NestedScrollConnection,
    textBackgroundColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    modifier: Modifier = Modifier,
    currentSongLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val density = LocalDensity.current

    val currentLyrics by playerConnection.currentLyrics.collectAsStateWithLifecycle(initialValue = null)

    val artistText = remember(mediaMetadata.artists) {
        if (mediaMetadata.artists.isNotEmpty()) {
            mediaMetadata.artists.joinToString(", ") { it.name }
        } else {
            "Unknown Artist"
        }
    }

    val lyricsHelper = remember(context) {
        try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                LyricsHelperEntryPoint::class.java,
            ).lyricsHelper()
        } catch (_: Exception) {
            null
        }
    }

    // Prefetch lyrics if lyrics view is opened and lyrics not available yet
    LaunchedEffect(mediaMetadata.id, isLyricsVisible, currentLyrics?.lyrics) {
        if (!isLyricsVisible || currentLyrics != null) return@LaunchedEffect
        try {
            val existingLyrics = withContext(Dispatchers.IO) {
                database.lyrics(mediaMetadata.id).first()
            }
            if (existingLyrics != null) return@LaunchedEffect

            lyricsHelper?.let { helper ->
                val lyrics = withContext(Dispatchers.IO) {
                    helper.getLyrics(mediaMetadata)
                }
                withContext(Dispatchers.IO) {
                    database.query {
                        insertLyricsIfAbsent(
                            id = mediaMetadata.id,
                            lyrics = lyrics,
                        )
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    // Emphasized motion curve for a continuous, smooth spatial transition without snapping or flashing
    val lyricsProgress by animateFloatAsState(
        targetValue = if (isLyricsVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 380,
            easing = FastOutSlowInEasing,
        ),
        label = "lyricsProgress",
    )

    val showLyricsMenu = {
        menuState.show {
            LyricsMenu(
                lyricsProvider = { currentLyrics },
                mediaMetadataProvider = { mediaMetadata },
                lyricsSyncOffset = lyricsSyncOffset,
                onLyricsSyncOffsetChange = onLyricsSyncOffsetChange,
                onDismiss = menuState::dismiss,
            )
        }
    }

    BackHandler(enabled = isLyricsVisible, onBack = onDismissLyrics)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val isLandscape = containerWidth > containerHeight

        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        val (thumbnailCornerRadius) = rememberPreference(ThumbnailCornerRadiusKey, defaultValue = 8f)
        val normalRadius = thumbnailCornerRadius.dp

        // 1. Normal (full-size, centered) geometry
        val availableHeight = (containerHeight - statusBarTop).coerceAtLeast(100.dp)
        val normalHorizontalPadding = 24.dp
        val normalSize = (containerWidth - normalHorizontalPadding * 2)
            .coerceAtMost(availableHeight - 24.dp)
            .coerceIn(200.dp, 360.dp)
        val normalX = (containerWidth - normalSize) / 2
        val normalY = statusBarTop + (availableHeight - normalSize) / 2

        // 2. Shrunk (docked top-left) geometry
        val shrunkSize = if (isLandscape) 48.dp else 56.dp
        val shrunkX = 20.dp
        val shrunkY = statusBarTop + (if (isLandscape) 8.dp else 12.dp)
        val shrunkRadius = (thumbnailCornerRadius * 0.75f).coerceIn(8f, 16f).dp

        // 3. Interpolated geometry for perfectly continuous movement
        val currentSize = lerp(normalSize, shrunkSize, lyricsProgress)
        val currentX = lerp(normalX, shrunkX, lyricsProgress)
        val currentY = lerp(normalY, shrunkY, lyricsProgress)
        val currentRadius = lerp(normalRadius, shrunkRadius, lyricsProgress)
        val currentElevation = lerp(8.dp, 4.dp, lyricsProgress)

        val lyricsTopPadding = shrunkY + shrunkSize + (if (isLandscape) 8.dp else 12.dp)

        // 4. Lyrics View: smoothly fades in and slides up
        if (lyricsProgress > 0.01f) {
            val lyricsAlpha = ((lyricsProgress - 0.2f) / 0.8f).coerceIn(0f, 1f)
            val lyricsTranslationYPx = with(density) { (16.dp * (1f - lyricsProgress)).toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = lyricsTopPadding)
                    .graphicsLayer {
                        alpha = lyricsAlpha
                        translationY = lyricsTranslationYPx
                    }
            ) {
                val lyricsMode by rememberEnumPreference(
                    key = LyricsModeKey,
                    defaultValue = LyricsMode.V2,
                )
                LyricsContent(
                    lyricsMode = lyricsMode,
                    sliderPositionProvider = sliderPositionProvider,
                    lyricsSyncOffset = lyricsSyncOffset,
                    textColor = textBackgroundColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                )
            }
        }

        // 5. Top Header: Track title, artist, options, and dismiss button next to the shrunken cover art
        if (lyricsProgress > 0.01f) {
            val headerAlpha = ((lyricsProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
            val headerTranslationXPx = with(density) { (12.dp * (1f - lyricsProgress)).toPx() }
            val headerStartOffset = shrunkX + shrunkSize + 14.dp
            val headerWidth = (containerWidth - headerStartOffset - 16.dp).coerceAtLeast(0.dp)

            Row(
                modifier = Modifier
                    .offset(x = headerStartOffset, y = shrunkY)
                    .width(headerWidth)
                    .height(shrunkSize)
                    .graphicsLayer {
                        alpha = headerAlpha
                        translationX = headerTranslationXPx
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = mediaMetadata.title.ifEmpty { "Unknown Title" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textBackgroundColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee()
                            .clickable(onClick = onTitleClick),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = artistText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textBackgroundColor.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee()
                            .clickable(onClick = onArtistClick),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    // Like button
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (currentSongLiked) R.drawable.favorite else R.drawable.favorite_border
                            ),
                            contentDescription = "Like",
                            tint = if (currentSongLiked) textBackgroundColor else textBackgroundColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Lyrics options menu button
                    IconButton(
                        onClick = showLyricsMenu,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_horiz),
                            contentDescription = "More Options",
                            tint = textBackgroundColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    // Close lyrics button (expands cover art back to center)
                    IconButton(
                        onClick = onDismissLyrics,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(textButtonColor.copy(alpha = 0.18f)),
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
        }

        // 6. Persistent Cover Art Card: never unmounts, never pops or flashes
        val artworkRequest = rememberOfflineArtworkImageRequest(mediaMetadata.thumbnailUrl)
        var totalDragDistance by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .offset(x = currentX, y = currentY)
                .size(currentSize)
                .shadow(
                    elevation = currentElevation,
                    shape = RoundedCornerShape(currentRadius),
                    clip = false,
                )
                .clip(RoundedCornerShape(currentRadius))
                .then(
                    if (lyricsProgress > 0.5f) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            role = Role.Button,
                            onClick = onDismissLyrics,
                        )
                    } else {
                        Modifier
                            .pointerInput(mediaMetadata.id) {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        val currentPos = playerConnection.player.currentPosition
                                        val dur = playerConnection.player.duration
                                        if (tapOffset.x > size.width / 2) {
                                            playerConnection.player.seekTo((currentPos + 5000L).coerceAtMost(dur))
                                        } else {
                                            playerConnection.player.seekTo((currentPos - 5000L).coerceAtLeast(0L))
                                        }
                                    }
                                )
                            }
                            .pointerInput(mediaMetadata.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (totalDragDistance < -80f) {
                                            playerConnection.player.seekToNext()
                                        } else if (totalDragDistance > 80f) {
                                            playerConnection.player.seekToPrevious()
                                        }
                                        totalDragDistance = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        totalDragDistance += dragAmount
                                    }
                                )
                            }
                    }
                )
        ) {
            AsyncImage(
                model = artworkRequest,
                contentDescription = mediaMetadata.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
