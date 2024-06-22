package com.example.energyhub.model

import kotlinx.datetime.Instant
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class OffsetDateTime(
    private val local: LocalDateTime,
    private val timezone: TimeZone,
) {
    fun toFractionalHours(midnight: Instant? = null): Float {
        val midnightInstant = midnight ?: this.getMidnightPreceding()
        return (this.toInstant() - midnightInstant).inWholeSeconds / (60f * 60f)
    }

    fun getMidnightPreceding(): Instant {
        return this.local.date.atStartOfDayIn(timezone)
    }

    fun toInstant(): Instant {
        return local.toInstant(timezone)
    }

    operator fun compareTo(other: OffsetDateTime): Int {
        val thisInst = this.toInstant()
        val otherInst = other.toInstant()
        return when {
            (thisInst < otherInst) -> -1
            (thisInst > otherInst) -> 1
            else -> 0 // (thisInst == otherInst)

        }
    }
}

fun List<OffsetDateTime>.toHours(): List<Float> {
    if (this.isEmpty()) {
        return listOf()
    }
    val midnight = this[0].getMidnightPreceding()
    return this.map { it.toFractionalHours(midnight) }
}

fun List<LocalDateTime>.toOffsetDateTime(timezone: TimeZone): List<OffsetDateTime> {
    return this.map { OffsetDateTime(it, timezone) }
}

fun Instant.toFractionalHours(timeZone: TimeZone): Float {
    return OffsetDateTime(this.toLocalDateTime(timeZone), timeZone).toFractionalHours()
}