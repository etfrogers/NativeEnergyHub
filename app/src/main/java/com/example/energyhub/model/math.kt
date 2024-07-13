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

operator fun MutableList<Float>.plusAssign(other: List<Number>) {
    assertEqualSizes(this, other)
    this.mapInPlacePairwise(other) { t, o -> t + o.toFloat() }
}

operator fun List<Number>.times(other: Number): List<Float> {
    return this.map { t -> other.toFloat() * t.toFloat() }
}

operator fun List<Number>.div(other: Number): List<Float> {
    return this.map { t -> t.toFloat() / other.toFloat() }
}

fun integratePowers(powers: List<Float>, timestamps: List<OffsetDateTime>): Float {
    if (powers.size != timestamps.size){
        throw IllegalArgumentException("Powers and timestamps must be the same size")
    }
    if (powers.isEmpty()){
        return 0f
    }
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

inline fun <T, U> MutableList<T>.mapInPlacePairwise(other: List<U>, mutator: (T, U) -> (T)) {
    if (this.size != other.size){
        throw IllegalArgumentException("Input lists must be of equal length")
    }
    for (i in this.indices){
        this[i] = mutator(this[i], other[i])
    }
}