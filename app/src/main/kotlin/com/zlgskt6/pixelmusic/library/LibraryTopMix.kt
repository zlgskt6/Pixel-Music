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

package com.zlgskt6.pixelmusic.library

import androidx.compose.runtime.Immutable
import com.google.common.collect.ImmutableList
import com.zlgskt6.pixelmusic.models.MediaMetadata

enum class LibraryTopMixId {
    DAILY,
    CHILL,
    FOCUS,
}

@Immutable
data class LibraryTopMix(
    val id: LibraryTopMixId,
    val tracks: ImmutableList<MediaMetadata>,
    val previewArtworkUrls: ImmutableList<String>,
)

@Immutable
data class GeneratedLibraryTopMix(
    val id: String,
    val title: String,
    val description: String,
    val tracks: ImmutableList<MediaMetadata>,
)
