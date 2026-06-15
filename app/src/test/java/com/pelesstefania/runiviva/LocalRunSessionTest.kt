package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.model.LocalRunSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LocalRunSessionTest {

    @Test
    fun localRunSessionStoresCorrectValues() {
        val run = LocalRunSession(
            runId = "run1",
            userId = "user1",
            date = "2026-06-14",
            startTime = 1000L,
            endTime = 2000L,
            durationSeconds = 600,
            distanceKm = 2.5,
            paceMinPerKm = 4.0,
            isSynced = false
        )

        assertEquals("run1", run.runId)
        assertEquals("user1", run.userId)
        assertEquals("2026-06-14", run.date)
        assertEquals(600, run.durationSeconds)
        assertEquals(2.5, run.distanceKm, 0.01)
        assertEquals(4.0, run.paceMinPerKm, 0.01)
        assertFalse(run.isSynced)
    }
}