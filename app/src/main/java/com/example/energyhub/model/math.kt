package com.example.energyhub.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.minutes

operator fun List<Float>.times(other: List<Long>): List<Float> {
     return this.zip(other).map { (t, o) -> o*t }
}

fun integratePowers(powers: List<Float>, timestamps: List<LocalDateTime>): Float {
    val n = timestamps.size
    // Use gaps in hours to get Wh from W
    var gapsHours = timestamps.slice(1..<n)
        .zip(timestamps.slice(0..<n-1)).map {
                (t1, t2) ->
            (t2.toInstant(UtcOffset.ZERO)-t1.toInstant(UtcOffset.ZERO)).inWholeHours
    }
    // assume first entry is the standard 5-minute interval.
    gapsHours = listOf((5.minutes.inWholeHours)) + gapsHours
    return (powers * gapsHours).sum()
}