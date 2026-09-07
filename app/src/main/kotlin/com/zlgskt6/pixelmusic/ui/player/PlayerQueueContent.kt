/*
 * Pixel Music (2026)
 * © zlgskt6 — github.com/zlgskt6
 * GPL-3.0 License | Contributors: see git history
 */

package com.zlgskt6.pixelmusic.ui.player

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zlgskt6.pixelmusic.LocalDatabase
import com.zlgskt6.pixelmusic.LocalPlayerConnection
import com.zlgskt6.pixelmusic.R
import com.zlgskt6.pixelmusic.constants.AutoLoadMoreKey
import com.zlgskt6.pixelmusic.constants.EnableHapticFeedbackKey
import com.zlgskt6.pixelmusic.constants.ListItemHeight
import com.zlgskt6.pixelmusic.constants.QueueEditLockKey
import com.zlgskt6.pixelmusic.db.entities.PlaylistEntity
import com.zlgskt6.pixelmusic.db.entities.PlaylistSongMap
import com.zlgskt6.pixelmusic.extensions.metadata
import com.zlgskt6.pixelmusic.extensions.move
import com.zlgskt6.pixelmusic.extensions.togglePlayPause
import com.zlgskt6.pixelmusic.extensions.toggleRepeatMode
import com.zlgskt6.pixelmusic.models.MediaMetadata
import com.zlgskt6.pixelmusic.ui.component.ActionPromptDialog
import com.zlgskt6.pixelmusic.ui.component.LocalBottomSheetPageState
import com.zlgskt6.pixelmusic.ui.component.LocalMenuState
import com.zlgskt6.pixelmusic.ui.component.TextFieldDialog
import com.zlgskt6.pixelmusic.ui.menu.AddToPlaylistDialog
import com.zlgskt6.pixelmusic.ui.menu.PlayerMenu
import com.zlgskt6.pixelmusic.ui.utils.ShowMediaInfo
import com.zlgskt6.pixelmusic.utils.makeTimeString
import com.zlgskt6.pixelmusic.utils.rememberPreference
import com.zlgskt6.pixelmusic.ui.utils.resize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDateTime

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, UnstableApi::class)
@Composable
fun PlayerQueueContent(
    navController: NavController,
    textColor: Color,
    textButtonColor: Color,
    iconButtonColor: Color,
    modifier: Modifier = Modifier,
    dominantColor: Color = Color.Unspecified,
    onDismiss: (() -> Unit)? = null,
    onShowLyrics: (() -> Unit)? = null,
    onShowSleepTimer: (() -> Unit)? = null,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val coroutineScope = rememberCoroutineScope()

    val (enableHapticFeedback) = rememberPreference(EnableHapticFeedbackKey, true)
    fun triggerClickHaptic() {
        if (enableHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
    var locked by rememberPreference(QueueEditLockKey, false)
    val togetherSessionState by playerConnection.service.togetherSessionState.collectAsState()
    val togetherForcesLock =
        togetherSessionState is com.zlgskt6.pixelmusic.together.TogetherSessionState.Joined &&
            (togetherSessionState as com.zlgskt6.pixelmusic.together.TogetherSessionState.Joined).role is com.zlgskt6.pixelmusic.together.TogetherRole.Guest
    val effectiveLocked = locked || togetherForcesLock

    val queueWindows by playerConnection.queueWindows.collectAsState()
    val mutableQueueWindows = remember { mutableStateListOf<Timeline.Window>() }
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val currentPlayingUid = remember(currentWindowIndex, queueWindows) {
        if (currentWindowIndex in queueWindows.indices) queueWindows[currentWindowIndex].uid else null
    }

    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()
    val queueTitle by playerConnection.queueTitle.collectAsStateWithLifecycle()
    val queueLength = remember(queueWindows) {
        queueWindows.sumOf { it.mediaItem.metadata?.duration?.toLong() ?: 0L }
    }

    var infiniteQueueEnabled by rememberPreference(AutoLoadMoreKey, false)
    val infiniteQueueLoading by playerConnection.service.infiniteQueueLoading.collectAsStateWithLifecycle()

    var selection by remember { mutableStateOf(false) }
    val selectedSongs = remember { mutableStateListOf<MediaMetadata>() }
    val selectedItems = remember { mutableStateListOf<Timeline.Window>() }

    fun clearSelection() {
        selection = false
        selectedSongs.clear()
        selectedItems.clear()
    }

    BackHandler(enabled = selection) {
        clearSelection()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var dismissJob: Job? by remember { mutableStateOf(null) }
    var showChoosePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateQueuePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var showClearConfirmDialog by rememberSaveable { mutableStateOf(false) }

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onGetSong = {
            selectedSongs.map {
                database.withTransaction {
                    insert(it)
                }
                it.id
            }
        },
        onDismiss = { showChoosePlaylistDialog = false },
        onAddComplete = { songCount, playlistNames ->
            val message = when {
                songCount == 1 && playlistNames.size == 1 -> context.getString(R.string.added_to_playlist, playlistNames.first())
                songCount > 1 && playlistNames.size == 1 -> context.getString(R.string.added_n_songs_to_playlist, songCount, playlistNames.first())
                songCount == 1 -> context.getString(R.string.added_to_n_playlists, playlistNames.size)
                else -> context.getString(R.string.added_n_songs_to_n_playlists, songCount, playlistNames.size)
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            clearSelection()
        },
    )

    if (showCreateQueuePlaylistDialog) {
        TextFieldDialog(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.queue_music),
                    contentDescription = null,
                )
            },
            title = { Text(text = stringResource(R.string.create_playlist)) },
            placeholder = { Text(text = stringResource(R.string.playlist_name)) },
            initialTextFieldValue = TextFieldValue(queueTitle ?: context.getString(R.string.queue)),
            isInputValid = { it.trim().isNotEmpty() && selectedSongs.isNotEmpty() },
            onDismiss = { showCreateQueuePlaylistDialog = false },
            onDone = onDone@{ rawPlaylistName ->
                val playlistName = rawPlaylistName.trim()
                val songs = selectedSongs.toList()
                if (playlistName.isEmpty() || songs.isEmpty()) return@onDone

                coroutineScope.launch(Dispatchers.IO) {
                    val playlist = PlaylistEntity(
                        name = playlistName,
                        bookmarkedAt = LocalDateTime.now(),
                        isEditable = true,
                    )

                    database.withTransaction {
                        insert(playlist)
                        songs.forEachIndexed { index, song ->
                            insert(song)
                            insert(
                                PlaylistSongMap(
                                    playlistId = playlist.id,
                                    songId = song.id,
                                    position = index,
                                    setVideoId = song.setVideoId,
                                ),
                            )
                        }
                    }

                    withContext(Dispatchers.Main) {
                        val message = if (songs.size == 1) {
                            context.getString(R.string.added_to_playlist, playlistName)
                        } else {
                            context.getString(R.string.added_n_songs_to_playlist, songs.size, playlistName)
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        clearSelection()
                    }
                }
            },
        )
    }

    if (showClearConfirmDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.clear_queue),
            onConfirm = {
                val windowsToRemove = if (currentWindowIndex in queueWindows.indices) {
                    queueWindows.filterIndexed { index, _ -> index != currentWindowIndex }
                } else {
                    emptyList()
                }

                if (windowsToRemove.isNotEmpty()) {
                    val sortedWindows = windowsToRemove.sortedBy { it.firstPeriodIndex }
                    var i = 0
                    sortedWindows.forEach { window ->
                        playerConnection.player.removeMediaItem(window.firstPeriodIndex - i++)
                    }
                    clearSelection()
                }

                if (infiniteQueueEnabled) {
                    infiniteQueueEnabled = false
                    playerConnection.service.onInfiniteQueueDisabled()
                }
                showClearConfirmDialog = false
            },
            onDismiss = { showClearConfirmDialog = false },
        ) {
            Text(
                text = stringResource(R.string.clear_queue_confirm),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    val onRemoveWithUndo: (Timeline.Window) -> Unit = { window ->
        val index = window.firstPeriodIndex
        playerConnection.player.removeMediaItem(index)
        dismissJob?.cancel()
        dismissJob = coroutineScope.launch {
            val snackbarResult = snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.removed_song_from_queue,
                    window.mediaItem.metadata?.title,
                ),
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short,
            )
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                playerConnection.player.addMediaItem(window.mediaItem)
                playerConnection.player.moveMediaItem(
                    playerConnection.player.mediaItemCount - 1,
                    index,
                )
            }
        }
    }

    val onRemoveMultipleWithUndo: (List<Timeline.Window>) -> Unit = { windows ->
        if (windows.isNotEmpty()) {
            val sortedWindows = windows.sortedBy { it.firstPeriodIndex }
            var i = 0
            sortedWindows.forEach { window ->
                playerConnection.player.removeMediaItem(window.firstPeriodIndex - i++)
            }
            dismissJob?.cancel()
            dismissJob = coroutineScope.launch {
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = if (windows.size == 1) {
                        context.getString(
                            R.string.removed_song_from_queue,
                            windows.first().mediaItem.metadata?.title,
                        )
                    } else {
                        context.getString(R.string.removed_n_songs_from_queue, windows.size)
                    },
                    actionLabel = context.getString(R.string.undo),
                    duration = SnackbarDuration.Short,
                )
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    sortedWindows.forEach { window ->
                        playerConnection.player.addMediaItem(window.mediaItem)
                        playerConnection.player.moveMediaItem(
                            playerConnection.player.mediaItemCount - 1,
                            window.firstPeriodIndex,
                        )
                    }
                }
            }
        }
    }

    val lazyListState = rememberLazyListState()
    var dragInfo: Pair<Int, Int>? by remember { mutableStateOf(null) }
    var shouldScrollToCurrent by remember { mutableStateOf(true) }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
    ) { from, to ->
        val currentDragInfo = dragInfo
        dragInfo = if (currentDragInfo == null) {
            from.index to to.index
        } else {
            currentDragInfo.first to to.index
        }

        val safeFrom = from.index.coerceIn(0, mutableQueueWindows.lastIndex)
        val safeTo = to.index.coerceIn(0, mutableQueueWindows.lastIndex)
        mutableQueueWindows.move(safeFrom, safeTo)

        if (enableHapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    LaunchedEffect(queueWindows) {
        mutableQueueWindows.apply {
            clear()
            addAll(queueWindows)
        }
    }

    LaunchedEffect(currentPlayingUid, shouldScrollToCurrent) {
        if (currentPlayingUid != null && shouldScrollToCurrent) {
            val indexInMutableList = mutableQueueWindows.indexOfFirst { it.uid == currentPlayingUid }
            if (indexInMutableList != -1) {
                lazyListState.animateScrollToItem(indexInMutableList)
            }
        }
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            dragInfo?.let { (from, to) ->
                val safeFrom = from.coerceIn(0, queueWindows.lastIndex)
                val safeTo = to.coerceIn(0, queueWindows.lastIndex)

                if (!playerConnection.player.shuffleModeEnabled) {
                    playerConnection.player.moveMediaItem(safeFrom, safeTo)
                } else {
                    playerConnection.localPlayer.setShuffleOrder(
                        DefaultShuffleOrder(
                            queueWindows
                                .map { it.firstPeriodIndex }
                                .toMutableList()
                                .move(safeFrom, safeTo)
                                .toIntArray(),
                            System.currentTimeMillis(),
                        ),
                    )
                }
                dragInfo = null
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val queueHeaderTopPadding = if (isLandscape) 10.dp else 44.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .fillMaxHeight()
                .padding(start = 20.dp, end = 20.dp, top = queueHeaderTopPadding, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Navigation / Header Row (matching Sleep Timer UI style)
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
                            .background(textColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.queue_music),
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.queue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                        )
                        Text(
                            text = if (queueWindows.isNotEmpty()) {
                                pluralStringResource(
                                    R.plurals.n_song,
                                    queueWindows.size,
                                    queueWindows.size,
                                ) + if (queueLength > 0) " • ${makeTimeString(queueLength * 1000)}" else ""
                            } else {
                                "Queue is empty"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Shuffle button in circular pill
                    val shuffleModeEnabled = playerConnection.player.shuffleModeEnabled
                    IconButton(
                        onClick = {
                            triggerClickHaptic()
                            coroutineScope.launch(Dispatchers.Main) {
                                playerConnection.player.shuffleModeEnabled = !playerConnection.player.shuffleModeEnabled
                            }
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    if (shuffleModeEnabled) textButtonColor.copy(alpha = 0.22f)
                                    else textColor.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (shuffleModeEnabled) R.drawable.shuffle_on else R.drawable.shuffle
                                ),
                                contentDescription = "Shuffle",
                                tint = if (shuffleModeEnabled) textColor else textColor.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Repeat button in circular pill
                    IconButton(
                        onClick = {
                            triggerClickHaptic()
                            playerConnection.player.toggleRepeatMode()
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    if (repeatMode != Player.REPEAT_MODE_OFF) textButtonColor.copy(alpha = 0.22f)
                                    else textColor.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(
                                    when (repeatMode) {
                                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one_on
                                        Player.REPEAT_MODE_ALL -> R.drawable.repeat_on
                                        else -> R.drawable.repeat
                                    }
                                ),
                                contentDescription = "Repeat",
                                tint = if (repeatMode != Player.REPEAT_MODE_OFF) textColor else textColor.copy(alpha = 0.75f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

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
                                    .background(textColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.lyrics),
                                    contentDescription = stringResource(R.string.lyrics),
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    if (onShowSleepTimer != null) {
                        IconButton(
                            onClick = {
                                triggerClickHaptic()
                                onShowSleepTimer()
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(textColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.bedtime),
                                    contentDescription = stringResource(R.string.sleep_timer),
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    if (onDismiss != null) {
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
                                    .background(textColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = "Close",
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Queue Card Surface (in the same transparent glassmorphic style as Sleep Timer!)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = textColor.copy(alpha = 0.08f),
                border = BorderStroke(
                    width = 1.dp,
                    color = textColor.copy(alpha = 0.14f),
                ),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Control Strip at the top of the card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Auto-load Radio Chip
                        FilterChip(
                            selected = infiniteQueueEnabled,
                            onClick = {
                                triggerClickHaptic()
                                val next = !infiniteQueueEnabled
                                infiniteQueueEnabled = next
                                if (next) {
                                    playerConnection.service.onInfiniteQueueEnabled()
                                } else {
                                    playerConnection.service.onInfiniteQueueDisabled()
                                }
                            },
                            label = {
                                if (infiniteQueueLoading) {
                                    CircularWavyProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = textColor,
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.radio),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.radio),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = textColor.copy(alpha = 0.06f),
                                labelColor = textColor.copy(alpha = 0.7f),
                                iconColor = textColor.copy(alpha = 0.7f),
                                selectedContainerColor = textButtonColor.copy(alpha = 0.22f),
                                selectedLabelColor = textColor,
                                selectedLeadingIconColor = textColor,
                            ),
                            border = null,
                        )

                        // Quick action buttons: Lock, Multi-select, Clear
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            // Lock toggle
                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    locked = !locked
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (effectiveLocked) textButtonColor.copy(alpha = 0.22f)
                                            else textColor.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            if (effectiveLocked) R.drawable.lock else R.drawable.lock_open,
                                        ),
                                        contentDescription = if (effectiveLocked) "Unlock Queue" else "Lock Queue",
                                        tint = if (effectiveLocked) textButtonColor else textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }

                            // Multi-select toggle
                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    selection = !selection
                                    if (!selection) clearSelection()
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selection) textButtonColor.copy(alpha = 0.22f)
                                            else textColor.copy(alpha = 0.08f)
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.select),
                                        contentDescription = "Multi-select",
                                        tint = if (selection) textButtonColor else textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }

                            // Clear queue
                            if (queueWindows.size > 1) {
                                IconButton(
                                    onClick = {
                                        triggerClickHaptic()
                                        showClearConfirmDialog = true
                                    },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(textColor.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.clear_all),
                                            contentDescription = stringResource(R.string.clear_queue),
                                            tint = textColor.copy(alpha = 0.75f),
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Subtle separator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(textColor.copy(alpha = 0.08f)),
                    )

                    // Scrollable List of Songs
                    if (mutableQueueWindows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(textColor.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.queue_music),
                                        contentDescription = null,
                                        tint = textColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Queue is empty",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = textColor.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Songs played next will appear here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.5f),
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                bottom = if (selection) 80.dp else 16.dp,
                                top = 6.dp,
                            ),
                        ) {
                itemsIndexed(
                    items = mutableQueueWindows,
                    key = { _, item -> item.uid.hashCode() },
                ) { index, window ->
                    ReorderableItem(
                        state = reorderableState,
                        key = window.uid.hashCode(),
                    ) {
                        val currentItem by rememberUpdatedState(window)
                        val isActive = window.uid == currentPlayingUid
                        val trackMetadata = window.mediaItem.metadata ?: return@ReorderableItem

                        val dismissBoxState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
                        )

                        var processedDismiss by remember { mutableStateOf(false) }
                        LaunchedEffect(dismissBoxState.currentValue) {
                            val dv = dismissBoxState.currentValue
                            if (!processedDismiss && (
                                    dv == SwipeToDismissBoxValue.StartToEnd ||
                                        dv == SwipeToDismissBoxValue.EndToStart
                                )
                            ) {
                                processedDismiss = true
                                onRemoveWithUndo(currentItem)
                            }
                            if (dv == SwipeToDismissBoxValue.Settled) {
                                processedDismiss = false
                            }
                        }

                        val rowContent = @Composable {
                            PlayerQueueListItem(
                                mediaMetadata = trackMetadata,
                                isActive = isActive,
                                isPlaying = isPlaying && isActive,
                                isSelected = selection && trackMetadata in selectedSongs,
                                isSelectionMode = selection,
                                isLocked = effectiveLocked,
                                textColor = textColor,
                                textButtonColor = textButtonColor,
                                dominantColor = dominantColor,
                                onClick = {
                                    if (selection) {
                                        if (trackMetadata in selectedSongs) {
                                            selectedSongs.remove(trackMetadata)
                                            selectedItems.remove(currentItem)
                                            if (selectedSongs.isEmpty()) {
                                                selection = false
                                            }
                                        } else {
                                            selectedSongs.add(trackMetadata)
                                            selectedItems.add(currentItem)
                                        }
                                    } else {
                                        if (index == currentWindowIndex) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.player.seekToDefaultPosition(window.firstPeriodIndex)
                                            playerConnection.player.playWhenReady = true
                                            shouldScrollToCurrent = false
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (enableHapticFeedback) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!selection) {
                                        selection = true
                                    }
                                    selectedSongs.clear()
                                    selectedItems.clear()
                                    selectedSongs.add(trackMetadata)
                                    selectedItems.add(currentItem)
                                },
                                onMoreClick = {
                                    menuState.show {
                                        PlayerMenu(
                                            mediaMetadata = trackMetadata,
                                            navController = navController,
                                            playerBottomSheetState = null,
                                            isQueueTrigger = true,
                                            onRemoveFromQueue = {
                                                onRemoveWithUndo(window)
                                            },
                                            onShowDetailsDialog = {
                                                window.mediaItem.mediaId.let {
                                                    bottomSheetPageState.show {
                                                        ShowMediaInfo(it)
                                                    }
                                                }
                                            },
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                                dragHandleModifier = Modifier.draggableHandle(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (effectiveLocked || selection) {
                            rowContent()
                        } else {
                            SwipeToDismissBox(
                                state = dismissBoxState,
                                backgroundContent = {
                                    val direction = dismissBoxState.dismissDirection
                                    if (direction != SwipeToDismissBoxValue.Settled) {
                                        val alignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp, vertical = 2.5.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f))
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = alignment,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.delete),
                                                contentDescription = stringResource(R.string.delete),
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                    }
                                },
                            ) {
                                rowContent()
                            }
                        }
                    }
                }
            }
        }
    }
}
        }

        // Undo & Status Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .padding(bottom = if (selection) 72.dp else 12.dp)
                .align(Alignment.BottomCenter),
        )

        // Multi-Selection Floating Toolbar
        AnimatedVisibility(
            visible = selection,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            QueueSelectionFloatingToolbar(
                allSelected = selectedSongs.size == mutableQueueWindows.size,
                pureBlack = false,
                onClose = ::clearSelection,
                onToggleSelectAll = {
                    if (selectedSongs.size == mutableQueueWindows.size) {
                        clearSelection()
                    } else {
                        selectedSongs.clear()
                        selectedItems.clear()
                        mutableQueueWindows.forEach { window ->
                            window.mediaItem.metadata?.let { metadata ->
                                selectedSongs.add(metadata)
                                selectedItems.add(window)
                            }
                        }
                    }
                },
                onAddToPlaylist = { showChoosePlaylistDialog = true },
                onCreatePlaylist = { showCreateQueuePlaylistDialog = true },
                onDelete = {
                    onRemoveMultipleWithUndo(selectedItems.toList())
                    clearSelection()
                },
            )
        }
    }
}

@Composable
fun AnimatedEqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bar1",
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bar2",
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bar3",
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        val h1 = if (isPlaying) bar1Height else 0.45f
        val h2 = if (isPlaying) bar2Height else 0.75f
        val h3 = if (isPlaying) bar3Height else 0.35f

        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h1)
                .clip(RoundedCornerShape(1.5.dp))
                .background(color),
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h2)
                .clip(RoundedCornerShape(1.5.dp))
                .background(color),
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h3)
                .clip(RoundedCornerShape(1.5.dp))
                .background(color),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerQueueListItem(
    mediaMetadata: MediaMetadata,
    isActive: Boolean,
    isPlaying: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isLocked: Boolean,
    textColor: Color,
    textButtonColor: Color,
    dominantColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    dragHandleModifier: Modifier = Modifier,
    shouldLoadImage: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val itemBackground = when {
        isSelected -> textButtonColor.copy(alpha = 0.35f)
        isActive -> Color.White.copy(alpha = 0.40f)
        else -> Color.White.copy(alpha = 0.25f)
    }

    val itemBorder = when {
        isSelected -> BorderStroke(1.5.dp, textButtonColor.copy(alpha = 0.8f))
        isActive -> BorderStroke(1.dp, Color.White.copy(alpha = 0.60f))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
    }

    val primaryTextColor = textColor
    val secondaryTextColor = textColor.copy(alpha = 0.75f)
    val iconColor = textColor.copy(alpha = 0.75f)
    val dragIconColor = textColor.copy(alpha = 0.55f)
    val thumbPlaceholderBg = Color.White.copy(alpha = 0.20f)
    val thumbPlaceholderIcon = textColor.copy(alpha = 0.50f)
    val equalizerColor = textColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.5.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(itemBorder, RoundedCornerShape(14.dp))
            .background(itemBackground)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        // Thumbnail & Status Indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(thumbPlaceholderBg),
            contentAlignment = Alignment.Center,
        ) {
            val artworkUrl = mediaMetadata.thumbnailUrl
            if (shouldLoadImage && !artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(artworkUrl.resize(256, 256))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.music_note),
                    contentDescription = null,
                    tint = thumbPlaceholderIcon,
                    modifier = Modifier.size(22.dp),
                )
            }

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isSelected) textButtonColor.copy(alpha = 0.85f)
                            else Color.Black.copy(alpha = 0.45f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(R.drawable.done),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            } else if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isPlaying) {
                        AnimatedEqualizerBars(
                            isPlaying = true,
                            color = equalizerColor,
                            modifier = Modifier.height(18.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.pause),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist/Duration
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = mediaMetadata.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                ),
                color = primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (isActive && isPlaying) Modifier.basicMarquee(iterations = Int.MAX_VALUE) else Modifier,
            )

            val artistNames = remember(mediaMetadata.artists) {
                mediaMetadata.artists.joinToString(", ") { it.name }
            }
            val durationText = remember(mediaMetadata.duration) {
                makeTimeString(mediaMetadata.duration * 1000L)
            }
            val subtitleText = remember(artistNames, durationText) {
                if (durationText.isNotBlank()) "$artistNames • $durationText" else artistNames
            }

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Trailing actions: More options & Drag Handle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = "Options",
                    tint = iconColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (!isLocked && !isSelectionMode) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(36.dp)
                        .then(dragHandleModifier),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.drag_handle),
                        contentDescription = "Reorder",
                        tint = dragIconColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

