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

package com.zlgskt6.pixelmusic.ai

import androidx.compose.runtime.Immutable
import com.zlgskt6.pixelmusic.constants.AiProvider

@Immutable
data class AiModelOption(
    val id: String,
    val displayName: String,
)

@Immutable
data class AiServiceConfig(
    val provider: AiProvider,
    val apiKey: String,
    val customEndpoint: String,
    val model: String,
) {
    val canCallApi: Boolean
        get() = provider != AiProvider.NONE &&
            apiKey.isNotBlank() &&
            (provider != AiProvider.CUSTOM || customEndpoint.isNotBlank())
}
