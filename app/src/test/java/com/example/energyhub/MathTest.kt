package com.example.energyhub

import com.example.energyhub.ui.screens.digitize

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream


/**
 *
 */
class MathTest {
    @ParameterizedTest
    @MethodSource("digitizeParams")
//    @ValueSource(shorts = shortArrayOf(listOf(1f, 1f, 3f, 3f), listOf(2f), listOf(0,0,1,1)))
    fun testDigitize(data: List<Float>, edges: List<Float>, expected: List<Int>) {
//    fun testDigitize(shorts: Short) {
        Assertions.assertEquals(expected, digitize(data, edges))
    }

    companion object {
        @JvmStatic
        fun digitizeParams(): Stream<Arguments> {
            return listOf(
                Arguments.of(listOf(1f, 1f, 3f, 3f), listOf(2f), listOf(0,0,1,1)),
                Arguments.of(listOf(1f, 1f, 3f, 3f), listOf(2f), listOf(0,0,1,1)),
            ).stream()
        }
    }
}