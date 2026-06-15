package com.pelesstefania.runiviva

import org.junit.Assert.assertEquals
import org.junit.Test

class RunCalculationTest {

    private fun calculatePaceValue(seconds: Long, meters: Double): Double {
        if (meters <= 0.0) return 0.0
        return (seconds / 60.0) / (meters / 1000.0)
    }

    private fun formatDistance(meters: Double): String {
        return String.format(java.util.Locale.getDefault(), "%.2f km", meters / 1000.0)
    }

    @Test
    fun calculatePaceForTwoKmInTenMinutes() {
        val pace = calculatePaceValue(
            seconds = 600,
            meters = 2000.0
        )

        assertEquals(5.0, pace, 0.01)
    }

    @Test
    fun calculatePaceReturnsZeroWhenDistanceIsZero() {
        val pace = calculatePaceValue(
            seconds = 600,
            meters = 0.0
        )

        assertEquals(0.0, pace, 0.01)
    }

    @Test
    fun formatDistanceMetersToKm() {
        val result = formatDistance(2530.0)

        assertEquals("2,53 km", result)
    }
}