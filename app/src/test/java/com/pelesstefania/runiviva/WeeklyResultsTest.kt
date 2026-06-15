package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.model.WeeklyChallengeEntry
import com.pelesstefania.runiviva.model.WeeklyResults
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyResultsTest {

    @Test
    fun weeklyResultsKeepsTopThreeAndUserRank() {
        val results = WeeklyResults(
            top3 = listOf(
                WeeklyChallengeEntry("1", "Ana", 15.0, 5000),
                WeeklyChallengeEntry("2", "Maria", 12.0, 4200),
                WeeklyChallengeEntry("3", "Ioana", 9.0, 3000)
            ),
            userRank = 2,
            userDistanceKm = 12.0
        )

        assertEquals(3, results.top3.size)
        assertEquals("Ana", results.top3[0].username)
        assertEquals(2, results.userRank)
        assertEquals(12.0, results.userDistanceKm, 0.01)
    }
}