/*
 * Pixel Music (2026)
 * © zlgskt6 — github.com/zlgskt6
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.zlgskt6.pixelmusic.extensions

import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.Modifier

fun Modifier.smartMarquee(enabled: Boolean = true): Modifier =
    if (enabled) this.basicMarquee() else this
