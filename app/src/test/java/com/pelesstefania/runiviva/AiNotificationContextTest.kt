package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.model.AiNotificationContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiNotificationContextTest {

    @Test
    fun contextStoresSickDayInformation() {
        val context = AiNotificationContext(
            username = "Antonia",
            notificationTone = "encouraging",
            ranToday = false,
            todayDistanceKm = 0.0,
            totalDistanceKm = 25.0,
            friendRunsLast7Days = 4,
            hasFriends = true
        )

        assertEquals("encouraging", context.notificationTone)
        assertFalse(context.ranToday)
        assertTrue(context.hasFriends)
        assertEquals(25.0, context.totalDistanceKm, 0.01)
    }
}