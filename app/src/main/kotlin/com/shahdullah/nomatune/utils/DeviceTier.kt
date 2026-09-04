/*
 * Pixel Music (2026)
 * © Shahdullah — github.com/shahdullah
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.shahdullah.nomatune.utils

import android.app.ActivityManager
import android.content.Context

fun Context.isMidRangeDevice(): Boolean {
    return try {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo)
            val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            totalRamGb in 0.1..4.5 // 4GB RAM devices or lower
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}
