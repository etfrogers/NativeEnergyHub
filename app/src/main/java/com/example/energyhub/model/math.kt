package com.example.energyhub.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

private fun assertEqualSizes(l1: List<Any>, l2: List<Any>){
    if (l1.size != l2.size){
        throw ArithmeticException("Operators are only defined for lists of equal size")
    }
}

operator fun List<Number>.times(other: List<Number>): List<Float> {
    assertEqualSizes(this, other)
    return this.zip(other).map { (t, o) -> o.toFloat() * t.toFloat() }
}

operator fun List<Number>.div(other: List<Number>): List<Float> {
    assertEqualSizes(this, other)
    return this.zip(other).map { (t, o) -> t.toFloat() / o.toFloat() }
}

operator fun List<Number>.minus(other: List<Number>): List<Float> {
    assertEqualSizes(this, other)
    return this.zip(other).map { (t, o) -> t.toFloat() - o.toFloat() }
}

operator fun List<Number>.plus(other: List<Number>): List<Float> {
    assertEqualSizes(this, other)
    return this.zip(other).map { (t, o) -> t.toFloat() + o.toFloat() }
}

operator fun List<Number>.times(other: Number): List<Float> {
    return this.map { t -> other.toFloat() * t.toFloat() }
}

fun List<LocalDateTime>.toHours(timeZone: TimeZone): List<Float> {
    if (this.isEmpty()) {
        return listOf()
    }
    val midnight = this[0].getMidnightPreceding(timeZone)
    return this.map { it.toFractionalHours(timeZone, midnight) }
}

private fun LocalDateTime.getMidnightPreceding(timeZone: TimeZone): Instant {
    return this.date.atStartOfDayIn(timeZone)
}

fun LocalDateTime.toFractionalHours(timeZone: TimeZone, midnight: Instant? = null): Float {
    val midnightInstant = midnight ?: this.getMidnightPreceding(timeZone)
    return (this.toInstant(timeZone) - midnightInstant).inWholeSeconds / (60f * 60f)
}

fun Instant.toFractionalHours(timeZone: TimeZone): Float {
    return this.toLocalDateTime(timeZone).toFractionalHours(timeZone)
}

fun integratePowers(powers: List<Float>, timestamps: List<LocalDateTime>): Float {
    // Use gaps in hours to get Wh from W
    val gapsHours = timestamps.drop(1)
        .zip(timestamps.dropLast(1)).map {
                (t1, t2) ->
            (t2.toInstant(UtcOffset.ZERO)-t1.toInstant(UtcOffset.ZERO)).inWholeHours
    }.toMutableList()
    // assume first entry is the standard 5-minute interval.
    gapsHours.add(0,  5.minutes.inWholeHours)
    return (powers * gapsHours).sum()
}

inline fun <T> MutableList<T>.mapInPlace(mutator: (T) -> (T)) {
    this.forEachIndexed { i, value ->
        val changedValue = mutator(value)
        if (value != changedValue) {
            this[i] = changedValue
        }
    }
}
