package com.example.energyhub.model

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

fun integratePowers(powers: List<Float>, timestamps: List<OffsetDateTime>): Float {
    // Use gaps in hours to get Wh from W
    val gapsHours = timestamps.drop(1)
        .zip(timestamps.dropLast(1)).map {
                (t1, t2) ->
            (t2.toInstant()-t1.toInstant()).inWholeHours
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
