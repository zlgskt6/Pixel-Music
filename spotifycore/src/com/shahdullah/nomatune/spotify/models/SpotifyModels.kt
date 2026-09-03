/*
 * Pixel Music (2026)
 * © Shahdullah — github.com/shahdullah
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Based on ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.shahdullah.nomatune.spotify.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyImage(
    val url: String = "",
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
data class SpotifySimpleArtist(
    val id: String = "",
    val name: String = "",
    val uri: String? = null,
)

@Serializable
data class SpotifySimpleAlbum(
    val id: String = "",
    val name: String = "",
    val images: List<SpotifyImage> = emptyList(),
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("album_type") val albumType: String? = null,
    val artists: List<SpotifySimpleArtist> = emptyList(),
    val uri: String? = null,
)

@Serializable
data class SpotifyTrack(
    val id: String = "",
    val name: String = "",
    val artists: List<SpotifySimpleArtist> = emptyList(),
    val album: SpotifySimpleAlbum? = null,
    @SerialName("duration_ms") val durationMs: Int = 0,
    val uri: String? = null,
    val explicit: Boolean = false,
    val popularity: Int? = null,
    @SerialName("track_number") val trackNumber: Int? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
    @SerialName("is_local") val isLocal: Boolean = false,
)

@Serializable
data class SpotifyPlaylistOwner(
    val id: String = "",
    @SerialName("display_name") val displayName: String? = null,
    val uri: String? = null,
)

@Serializable
data class SpotifyPlaylistTracksRef(
    val href: String? = null,
    val total: Int? = null,
)

@Serializable
data class SpotifyPlaylist(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val owner: SpotifyPlaylistOwner? = null,
    val tracks: SpotifyPlaylistTracksRef? = null,
    val uri: String? = null,
    val collaborative: Boolean = false,
    val public: Boolean? = null,
    val snapshotId: String? = null,
)

@Serializable
data class SpotifyPlaylistTrack(
    val track: SpotifyTrack? = null,
    val uid: String? = null,
    @SerialName("added_at") val addedAt: String? = null,
    @SerialName("is_local") val isLocal: Boolean = false,
)

@Serializable
data class SpotifySavedTrack(
    @SerialName("added_at") val addedAt: String? = null,
    val track: SpotifyTrack,
)

@Serializable
data class SpotifyUser(
    val id: String = "",
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val uri: String? = null,
)

@Serializable
data class SpotifyPaging<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    val href: String? = null,
    val next: String? = null,
    val previous: String? = null,
)

@Serializable
data class SpotifyRecommendations(
    val tracks: List<SpotifyTrack> = emptyList(),
    val seeds: List<SpotifyRecommendationSeed> = emptyList(),
)

@Serializable
data class SpotifyRecommendationSeed(
    val id: String = "",
    val initialPoolSize: Int = 0,
    val afterFilteringSize: Int = 0,
    val afterRelinkingSize: Int = 0,
    val href: String? = null,
    val type: String? = null,
)

@Serializable
data class SpotifySearchResult(
    val tracks: SpotifyPaging<SpotifyTrack>? = null,
    val albums: SpotifyPaging<SpotifyAlbum>? = null,
    val artists: SpotifyPaging<SpotifyArtist>? = null,
    val playlists: SpotifyPaging<SpotifyPlaylist>? = null,
)

@Serializable
data class SpotifyInternalToken(
    val accessToken: String = "",
    val accessTokenExpirationTimestampMs: Long = 0L,
    val isAnonymous: Boolean = false,
)
