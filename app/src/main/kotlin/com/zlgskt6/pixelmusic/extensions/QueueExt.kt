/*
 * Pixel Music (2026)
 * © zlgskt6 — github.com/zlgskt6
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Based on ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.zlgskt6.pixelmusic.extensions

import com.zlgskt6.pixelmusic.innertube.models.WatchEndpoint
import com.zlgskt6.pixelmusic.models.PersistQueue
import com.zlgskt6.pixelmusic.models.QueueData
import com.zlgskt6.pixelmusic.models.QueueType
import com.zlgskt6.pixelmusic.models.MediaMetadata
import com.zlgskt6.pixelmusic.playback.queues.ListQueue
import com.zlgskt6.pixelmusic.playback.queues.LocalAlbumRadio
import com.zlgskt6.pixelmusic.playback.queues.Queue
import com.zlgskt6.pixelmusic.playback.queues.YouTubeAlbumRadio
import com.zlgskt6.pixelmusic.playback.queues.YouTubeQueue

fun Queue.toPersistQueue(
    title: String?,
    items: List<MediaMetadata>,
    mediaItemIndex: Int,
    position: Long,
): PersistQueue = when (this) {
    is ListQueue -> PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.LIST,
    )
    is YouTubeQueue -> PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.YOUTUBE,
        queueData = QueueData.YouTubeData(
            videoId = endpoint.videoId,
            playlistId = endpoint.playlistId,
            endpointParams = endpoint.params,
            followAutomixPreview = followAutomixPreview,
        ),
    )
    is YouTubeAlbumRadio -> PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.YOUTUBE_ALBUM_RADIO,
        queueData = QueueData.YouTubeAlbumRadioData(
            playlistId = playlistId,
            albumSongCount = albumSongCount,
            continuation = continuation,
            firstTimeLoaded = firstTimeLoaded,
        ),
    )
    is LocalAlbumRadio -> PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.LOCAL_ALBUM_RADIO,
        queueData = QueueData.LocalAlbumRadioData(
            albumId = albumWithSongs.album.id,
            startIndex = startIndex,
        ),
    )
    else -> PersistQueue(
        title = title,
        items = items,
        mediaItemIndex = mediaItemIndex,
        position = position,
        queueType = QueueType.LIST,
    )
}

fun PersistQueue.toQueue(): Queue = ListQueue(
    title = title,
    items = items.map { it.toMediaItem() },
    startIndex = mediaItemIndex,
    position = position,
)

fun PersistQueue.toContinuationQueue(): Queue = when (queueType) {
    is QueueType.LIST -> ListQueue(
        title = title,
        items = items.map { it.toMediaItem() },
        startIndex = mediaItemIndex,
        position = position,
    )
    is QueueType.YOUTUBE -> {
        val data = queueData as? QueueData.YouTubeData
            ?: return ListQueue(title, items.map { it.toMediaItem() }, mediaItemIndex, position)
        YouTubeQueue(
            endpoint = WatchEndpoint(
                videoId = data.videoId,
                playlistId = data.playlistId,
                params = data.endpointParams,
            ),
            followAutomixPreview = data.followAutomixPreview,
        )
    }
    is QueueType.YOUTUBE_ALBUM_RADIO -> {
        val data = queueData as? QueueData.YouTubeAlbumRadioData
            ?: return ListQueue(title, items.map { it.toMediaItem() }, mediaItemIndex, position)
        YouTubeAlbumRadio(
            playlistId = data.playlistId,
            albumSongCount = data.albumSongCount,
            continuation = data.continuation,
            firstTimeLoaded = data.firstTimeLoaded,
        )
    }
    is QueueType.LOCAL_ALBUM_RADIO -> ListQueue(
        title = title,
        items = items.map { it.toMediaItem() },
        startIndex = mediaItemIndex,
        position = position,
    )
}
