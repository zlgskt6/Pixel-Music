package com.zlgskt6.pixelmusic.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.zlgskt6.pixelmusic.LocalDatabase
import com.zlgskt6.pixelmusic.LocalPlayerConnection
import com.zlgskt6.pixelmusic.R
import com.zlgskt6.pixelmusic.constants.LyricsMode
import com.zlgskt6.pixelmusic.constants.LyricsModeKey
import com.zlgskt6.pixelmusic.constants.ThumbnailCornerRadiusKey
import com.zlgskt6.pixelmusic.di.LyricsHelperEntryPoint
import com.zlgskt6.pixelmusic.extensions.toggleRepeatMode
import com.zlgskt6.pixelmusic.models.MediaMetadata
import com.zlgskt6.pixelmusic.ui.component.LocalMenuState
import com.zlgskt6.pixelmusic.ui.menu.LyricsMenu
import com.zlgskt6.pixelmusic.utils.rememberEnumPreference
import com.zlgskt6.pixelmusic.utils.rememberPreference
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Submenus available within the player artwork viewport.
 * Order determines sweep direction when hopping between views.
 */
enum class PlayerSubmenu(val order: Int) {
    NONE(-1),
    LYRICS(0),
    QUEUE(1),
    SLEEP_TIMER(2),
}

/**
 * Integrated player view that smoothly transitions between full-size centered cover art,
 * inline synced lyrics, queue list with docked cover art, and the sleep timer window.
 * Supports fluid sweeping animations and gestures when hopping between menus.
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
    dominantColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    currentSongLiked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onTitleClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
    onCollapse: (() -> Unit)? = null,
    isSleepTimerVisible: Boolean = false,
    onDismissSleepTimer: () -> Unit = {},
    isQueueVisible: Boolean = false,
    onDismissQueue: () -> Unit = {},
    navController: NavController? = null,
    onShowLyrics: () -> Unit = {},
    onShowQueue: () -> Unit = {},
    onShowSleepTimer: () -> Unit = {},
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val currentLyrics by playerConnection.currentLyrics.collectAsStateWithLifecycle(initialValue = null)
    val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()

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

    // Determine current active submenu for coordinated sweeping transitions
    val currentSubmenu = when {
        isSleepTimerVisible -> PlayerSubmenu.SLEEP_TIMER
        isQueueVisible -> PlayerSubmenu.QUEUE
        isLyricsVisible -> PlayerSubmenu.LYRICS
        else -> PlayerSubmenu.NONE
    }

    // Synchronously track previous submenu during composition without 1-frame LaunchedEffect lag
    var lastSubmenu by remember { mutableStateOf(currentSubmenu) }
    var previousSubmenu by remember { mutableStateOf(PlayerSubmenu.NONE) }
    if (lastSubmenu != currentSubmenu) {
        previousSubmenu = lastSubmenu
        lastSubmenu = currentSubmenu
    }

    val isFromSubmenuToLyrics = (previousSubmenu == PlayerSubmenu.QUEUE || previousSubmenu == PlayerSubmenu.SLEEP_TIMER) && currentSubmenu == PlayerSubmenu.LYRICS

    // Persistent docked state for cover art (docked ONLY during lyrics submenu, expands back when closing lyrics)
    val isDockedTarget = currentSubmenu == PlayerSubmenu.LYRICS
    val dockedProgress by animateFloatAsState(
        targetValue = if (isDockedTarget) 1f else 0f,
        animationSpec = tween(
            durationMillis = 360,
            easing = FastOutSlowInEasing,
        ),
        label = "dockedProgress",
    )

    // Replace cover art progress smoothly controls cover art fade for Sleep Timer and Queue
    val isOverlayReplaceTarget = currentSubmenu == PlayerSubmenu.SLEEP_TIMER || currentSubmenu == PlayerSubmenu.QUEUE
    val overlayReplaceProgress by animateFloatAsState(
        targetValue = if (isOverlayReplaceTarget) 1f else 0f,
        animationSpec = if (isOverlayReplaceTarget) {
            // Opening Queue / Sleep Timer: fast clean fade out of cover art first
            tween(
                durationMillis = 200,
                easing = FastOutLinearInEasing,
            )
        } else {
            // Dismissing Queue / Sleep Timer: wait for dismissal animation to complete, then fade in cover art
            tween(
                durationMillis = 260,
                delayMillis = 280,
                easing = LinearOutSlowInEasing,
            )
        },
        label = "overlayReplaceProgress",
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

    BackHandler(enabled = isSleepTimerVisible, onBack = onDismissSleepTimer)
    BackHandler(enabled = isQueueVisible && !isSleepTimerVisible, onBack = onDismissQueue)
    BackHandler(enabled = isLyricsVisible && !isSleepTimerVisible && !isQueueVisible, onBack = onDismissLyrics)

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

        // 3. Interpolated geometry for perfectly continuous movement without flicker
        val currentSize = lerp(normalSize, shrunkSize, dockedProgress)
        val currentX = lerp(normalX, shrunkX, dockedProgress)
        val currentY = lerp(normalY, shrunkY, dockedProgress)
        val currentRadius = lerp(normalRadius, shrunkRadius, dockedProgress)
        val baseElevation = lerp(8.dp, 4.dp, dockedProgress)
        val currentElevation = baseElevation * (1f - overlayReplaceProgress).coerceIn(0f, 1f)

        val contentTopPadding = shrunkY + shrunkSize + (if (isLandscape) 8.dp else 12.dp)

        // 4. Top Drag Handle Bar (Swipe down to hide/collapse player indicator)
        val dragHandleAlpha = ((1f - dockedProgress * 2.5f) * (1f - overlayReplaceProgress * 2.5f)).coerceIn(0f, 1f)
        if (dragHandleAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusBarTop + 10.dp)
                    .graphicsLayer { alpha = dragHandleAlpha }
                    .size(width = 56.dp, height = 24.dp)
                    .then(
                        if (onCollapse != null && isPlayerExpanded && dockedProgress < 0.1f && overlayReplaceProgress < 0.1f) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClickLabel = "Collapse player",
                                onClick = onCollapse,
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 5.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(textBackgroundColor.copy(alpha = 0.42f))
                )
            }
        }

        // 5. Coordinated Sweeping Animated Content for Submenus (Lyrics, Queue, Sleep Timer)
        AnimatedContent(
            targetState = currentSubmenu,
            transitionSpec = {
                val forward = targetState.order > initialState.order
                if (initialState == PlayerSubmenu.NONE) {
                    if (targetState == PlayerSubmenu.QUEUE || targetState == PlayerSubmenu.SLEEP_TIMER) {
                        // Opening Queue or Sleep Timer: wait for centered cover art to fade out first, then slide & fade in
                        (slideInVertically(
                            animationSpec = tween(320, delayMillis = 200, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                            initialOffsetY = { (it * 0.16f).toInt() }
                        ) + fadeIn(
                            animationSpec = tween(260, delayMillis = 200, easing = LinearOutSlowInEasing)
                        ) + scaleIn(
                            animationSpec = tween(320, delayMillis = 200, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                            initialScale = 0.94f
                        )).togetherWith(
                            fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                        )
                    } else {
                        // Opening lyrics from centered artwork: smooth vertical slide & fade
                        (slideInVertically(
                            animationSpec = tween(380, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                            initialOffsetY = { (it * 0.16f).toInt() }
                        ) + fadeIn(
                            animationSpec = tween(320, delayMillis = 30, easing = LinearOutSlowInEasing)
                        ) + scaleIn(
                            animationSpec = tween(380, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                            initialScale = 0.94f
                        )).togetherWith(
                            fadeOut(animationSpec = tween(220, easing = FastOutLinearInEasing))
                        )
                    }
                } else if (targetState == PlayerSubmenu.NONE) {
                    // Closing submenu back to centered artwork
                    fadeIn(animationSpec = tween(220)).togetherWith(
                        slideOutVertically(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            targetOffsetY = { (it * 0.14f).toInt() }
                        ) + fadeOut(
                            animationSpec = tween(260, easing = FastOutLinearInEasing)
                        ) + scaleOut(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            targetScale = 0.94f
                        )
                    )
                } else {
                    // Horizontal sweeping motion between submenus!
                    val sweepMultiplier = when {
                        targetState == PlayerSubmenu.LYRICS -> 1
                        initialState == PlayerSubmenu.LYRICS -> -1
                        targetState == PlayerSubmenu.SLEEP_TIMER && initialState == PlayerSubmenu.QUEUE -> 1
                        targetState == PlayerSubmenu.QUEUE && initialState == PlayerSubmenu.SLEEP_TIMER -> -1
                        else -> if (forward) 1 else -1
                    }
                    (slideInHorizontally(
                        animationSpec = tween(380, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                        initialOffsetX = { fullWidth -> (fullWidth * 0.85f).toInt() * sweepMultiplier }
                    ) + fadeIn(
                        animationSpec = tween(300, delayMillis = 40, easing = LinearOutSlowInEasing)
                    ) + scaleIn(
                        animationSpec = tween(380, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                        initialScale = 0.93f
                    )).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(360, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                            targetOffsetX = { fullWidth -> -(fullWidth * 0.85f).toInt() * sweepMultiplier }
                        ) + fadeOut(
                            animationSpec = tween(240, easing = FastOutLinearInEasing)
                        ) + scaleOut(
                            animationSpec = tween(360, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                            targetScale = 0.93f
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "PlayerSubmenuSweep",
        ) { submenu ->
            when (submenu) {
                PlayerSubmenu.NONE -> {
                    // Empty when in normal artwork mode
                    Spacer(Modifier.fillMaxSize())
                }

                PlayerSubmenu.LYRICS -> {
                    var lyricsSwipeTotal by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (lyricsSwipeTotal < -70f) {
                                            // Sweeping left hops to Queue
                                            onShowQueue()
                                        } else if (lyricsSwipeTotal > 70f) {
                                            // Sweeping right hops to Sleep Timer
                                            onShowSleepTimer()
                                        }
                                        lyricsSwipeTotal = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        lyricsSwipeTotal += dragAmount
                                    }
                                )
                            }
                    ) {
                        // Inline synced lyrics body
                        val (lyricsMode) = rememberEnumPreference(LyricsModeKey, defaultValue = LyricsMode.V2)
                        LyricsContent(
                            lyricsMode = lyricsMode,
                            sliderPositionProvider = sliderPositionProvider,
                            lyricsSyncOffset = lyricsSyncOffset,
                            textColor = textBackgroundColor,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = contentTopPadding),
                        )

                        // Top header aligned with docked cover art thumbnail inside the sweeping layout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .offset(x = shrunkX, y = shrunkY)
                                .width((containerWidth - shrunkX - 16.dp).coerceAtLeast(0.dp))
                                .height(shrunkSize),
                        ) {
                            // Docked Cover Art Thumbnail inside the sweeping layout
                            val artworkRequest = rememberOfflineArtworkImageRequest(mediaMetadata.thumbnailUrl)
                            Box(
                                modifier = Modifier
                                    .size(shrunkSize)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(shrunkRadius),
                                        ambientColor = Color.Black.copy(alpha = 0.15f),
                                        spotColor = Color.Black.copy(alpha = 0.25f),
                                    )
                                    .clip(RoundedCornerShape(shrunkRadius))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                        role = Role.Button,
                                        onClick = onDismissLyrics,
                                    )
                            ) {
                                AsyncImage(
                                    model = artworkRequest,
                                    contentDescription = mediaMetadata.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = mediaMetadata.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
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
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                // Hop to Queue button
                                IconButton(
                                    onClick = onShowQueue,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.queue_music),
                                        contentDescription = stringResource(R.string.queue),
                                        tint = textBackgroundColor.copy(alpha = 0.75f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }

                                // Hop to Sleep Timer button
                                IconButton(
                                    onClick = onShowSleepTimer,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.bedtime),
                                        contentDescription = stringResource(R.string.sleep_timer),
                                        tint = textBackgroundColor.copy(alpha = 0.75f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }

                                // More options menu
                                IconButton(
                                    onClick = showLyricsMenu,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_horiz),
                                        contentDescription = "More",
                                        tint = textBackgroundColor.copy(alpha = 0.65f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }

                                // Close lyrics button
                                IconButton(
                                    onClick = onDismissLyrics,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(textButtonColor.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.close),
                                            contentDescription = "Close",
                                            tint = textBackgroundColor,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PlayerSubmenu.QUEUE -> {
                    var queueSwipeTotal by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = statusBarTop, bottom = 8.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (queueSwipeTotal > 70f) {
                                            // Sweeping right hops to Lyrics
                                            onShowLyrics()
                                        } else if (queueSwipeTotal < -70f) {
                                            // Sweeping left hops to Sleep Timer
                                            onShowSleepTimer()
                                        }
                                        queueSwipeTotal = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        queueSwipeTotal += dragAmount
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (navController != null) {
                            PlayerQueueContent(
                                navController = navController,
                                textColor = textBackgroundColor,
                                textButtonColor = textButtonColor,
                                iconButtonColor = iconButtonColor,
                                dominantColor = dominantColor,
                                onDismiss = onDismissQueue,
                                onShowLyrics = onShowLyrics,
                                onShowSleepTimer = onShowSleepTimer,
                                nestedScrollConnection = nestedScrollConnection,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }

                PlayerSubmenu.SLEEP_TIMER -> {
                    var sleepTimerSwipeTotal by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = statusBarTop, bottom = 8.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (sleepTimerSwipeTotal > 70f) {
                                            // Sweeping right hops to Queue
                                            onShowQueue()
                                        } else if (sleepTimerSwipeTotal < -70f) {
                                            // Sweeping left hops to Lyrics
                                            onShowLyrics()
                                        }
                                        sleepTimerSwipeTotal = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        sleepTimerSwipeTotal += dragAmount
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        PlayerSleepTimerContent(
                            playerConnection = playerConnection,
                            textBackgroundColor = textBackgroundColor,
                            textButtonColor = textButtonColor,
                            iconButtonColor = iconButtonColor,
                            onDismiss = onDismissSleepTimer,
                            onShowLyrics = onShowLyrics,
                            onShowQueue = onShowQueue,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        // 6. Persistent Cover Art Card:
        // - In normal playback (NONE): full-size and centered.
        // - When user opens Lyrics from normal playback (NONE -> LYRICS): smoothly shrinks & moves up to top-left corner.
        // - When user closes Lyrics to normal playback (LYRICS -> NONE): smoothly expands back down to center.
        // - When in Queue or Sleep Timer, or hopping between submenus: hidden (alpha = 0), allowing pure horizontal sweep transitions.
        val artworkRequest = rememberOfflineArtworkImageRequest(mediaMetadata.thumbnailUrl)
        var totalDragDistance by remember { mutableFloatStateOf(0f) }

        val density = LocalDensity.current
        val isEnteringOverlayFromMediaPlayer = previousSubmenu == PlayerSubmenu.NONE && (currentSubmenu == PlayerSubmenu.QUEUE || currentSubmenu == PlayerSubmenu.SLEEP_TIMER)
        val sweepTranslationY = if (isEnteringOverlayFromMediaPlayer) {
            with(density) { (-16.dp * overlayReplaceProgress).toPx() }
        } else {
            0f
        }

        val floatingAlpha = when {
            currentSubmenu == PlayerSubmenu.LYRICS -> if (!isFromSubmenuToLyrics) (1f - overlayReplaceProgress).coerceIn(0f, 1f) else 0f
            currentSubmenu == PlayerSubmenu.NONE -> (1f - overlayReplaceProgress).coerceIn(0f, 1f)
            isEnteringOverlayFromMediaPlayer -> (1f - overlayReplaceProgress).coerceIn(0f, 1f)
            else -> 0f
        }
        val artworkScale = (1f - 0.04f * overlayReplaceProgress).coerceIn(0.8f, 1f)

        if (floatingAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .offset(x = currentX, y = currentY)
                    .size(currentSize)
                    .graphicsLayer {
                        alpha = floatingAlpha
                        translationY = sweepTranslationY
                        scaleX = artworkScale
                        scaleY = artworkScale
                        shadowElevation = (currentElevation * floatingAlpha).toPx()
                        shape = RoundedCornerShape(currentRadius)
                        clip = false
                        ambientShadowColor = Color.Black.copy(alpha = 0.15f * floatingAlpha)
                        spotShadowColor = Color.Black.copy(alpha = 0.25f * floatingAlpha)
                    }
                    .clip(RoundedCornerShape(currentRadius))
                    .then(
                        if (overlayReplaceProgress > 0.1f) {
                            Modifier
                        } else if (currentSubmenu == PlayerSubmenu.LYRICS) {
                            Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    role = Role.Button,
                                    onClick = onDismissLyrics,
                                )
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (totalDragDistance < -60f) onShowQueue()
                                            else if (totalDragDistance > 60f) onShowSleepTimer()
                                            totalDragDistance = 0f
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDragDistance += dragAmount
                                        }
                                    )
                                }
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
}
