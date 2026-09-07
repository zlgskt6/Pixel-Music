package com.zlgskt6.pixelmusic.unison.models

import kotlinx.serialization.Serializable

@Serializable
data class UnisonResponse(
    val success: Boolean = false,
    val data: UnisonEntry? = null,
    val message: String? = null,
)

@Serializable
data class UnisonSearchResponse(
    val success: Boolean = false,
    val data: List<UnisonEntry> = emptyList(),
    val message: String? = null,
)

@Serializable
data class UnisonEntry(
    val id: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null,
    val lyrics: String = "",
    val format: String? = null,
    val syncType: String? = null,
)
