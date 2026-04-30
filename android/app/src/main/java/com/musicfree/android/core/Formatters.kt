package com.musicfree.android.core

import kotlin.math.roundToInt

fun normalizeArtwork(url: String?): String? {
    if (url.isNullOrBlank()) {
        return null
    }
    return url
        .replace("http://", "https://")
        .replace(" ", "%20")
}

fun formatPlayCount(count: Long?): String {
    val safe = count ?: return "0"
    return when {
        safe >= 100_000_000L -> "${((safe / 100_000_000f) * 10f).roundToInt() / 10f}亿"
        safe >= 10_000L -> "${((safe / 10_000f) * 10f).roundToInt() / 10f}万"
        else -> safe.toString()
    }
}

fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) {
        return "00:00"
    }
    val totalSeconds = durationMs / 1_000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
