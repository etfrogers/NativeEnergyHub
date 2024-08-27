package com.example.energyhub.model

import kotlinx.datetime.Instant
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun List<Instant>.toHours(timezone: TimeZone): List<Float> {
    if (this.isEmpty()) {
        return listOf()
    }
    val midnight = this[0].getMidnightPreceding(timezone)
    return this.map { it.toFractionalHours(timezone, midnight) }
}

fun Instant.toFractionalHours(timezone: TimeZone, midnight: Instant? = null): Float {
    val midnightInstant = midnight ?: this.getMidnightPreceding(timezone)
    return (this - midnightInstant).inWholeSeconds / (60f * 60f)
}

fun Instant.getMidnightPreceding(timezone: TimeZone): Instant {
    return this.toLocalDateTime(timezone).date.atStartOfDayIn(timezone)
}