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

package com.zlgskt6.pixelmusic.lyrics

import android.content.Context
import com.zlgskt6.pixelmusic.constants.EnableSimpMusicLyricsKey
import com.zlgskt6.pixelmusic.simpmusic.SimpMusicLyrics
import com.zlgskt6.pixelmusic.utils.dataStore
import com.zlgskt6.pixelmusic.utils.get

object SimpMusicLyricsProvider : LyricsProvider {
    override val name: String = "SimpMusic"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableSimpMusicLyricsKey] ?: true

    private fun isYouTubeId(id: String): Boolean = id.matches(Regex("[A-Za-z0-9_-]{11}"))

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> {
        if (!isYouTubeId(id)) return Result.failure(
            IllegalStateException("SimpMusic: not a YouTube ID"))
        return SimpMusicLyrics.getLyrics(videoId = id, duration = duration)
    }

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        if (!isYouTubeId(id)) return
        SimpMusicLyrics.getAllLyrics(videoId = id, duration = duration, callback = callback)
    }
}

