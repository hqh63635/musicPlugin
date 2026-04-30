package com.musicfree.android.player

import com.musicfree.android.data.model.LyricLine

object LyricParser {
    private val pattern = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})]")

    fun parse(rawLrc: String): List<LyricLine> {
        if (rawLrc.isBlank()) {
            return emptyList()
        }
        return rawLrc.lineSequence().flatMap { line ->
            val matches = pattern.findAll(line).toList()
            val text = line.replace(pattern, "").trim()
            if (matches.isEmpty() || text.isBlank()) {
                emptyList()
            } else {
                matches.map { match ->
                    val minute = match.groupValues[1].toLongOrNull() ?: 0L
                    val second = match.groupValues[2].toLongOrNull() ?: 0L
                    val millis = match.groupValues[3]
                        .padEnd(3, '0')
                        .take(3)
                        .toLongOrNull()
                        ?: 0L
                    LyricLine(
                        timeMs = minute * 60_000 + second * 1_000 + millis,
                        text = text,
                    )
                }
            }
        }.sortedBy { it.timeMs }.toList()
    }

    fun findCurrentIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) {
            return -1
        }
        val index = lines.indexOfLast { it.timeMs <= positionMs }
        return if (index < 0) 0 else index
    }
}
