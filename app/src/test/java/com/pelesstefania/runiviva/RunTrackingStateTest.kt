package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.service.RunTrackingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunTrackingStateTest {

    @Test
    fun defaultStateIsNotRunning() {
        val state = RunTrackingState()

        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertEquals(0L, state.elapsedSeconds)
        assertEquals(0.0, state.distanceMeters, 0.001)
        assertEquals(0L, state.startTimeMillis)
    }

    @Test
    fun pausedStateKeepsRunningTrue() {
        val state = RunTrackingState(
            isRunning = true,
            isPaused = true,
            elapsedSeconds = 120L,
            distanceMeters = 500.0,
            startTimeMillis = 1000L
        )

        assertTrue(state.isRunning)
        assertTrue(state.isPaused)
        assertEquals(120L, state.elapsedSeconds)
        assertEquals(500.0, state.distanceMeters, 0.001)
    }
}