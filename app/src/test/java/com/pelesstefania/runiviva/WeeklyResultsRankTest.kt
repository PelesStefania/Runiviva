package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.model.WeeklyChallengeEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyResultsRankTest {

    @Test
    fun userRankIsCalculatedCorrectly() {
        val currentUserId = "user2"

        val entries = listOf(
            WeeklyChallengeEntry("user1", "Ana", 15.0, 5000),
            WeeklyChallengeEntry("user2", "Antonia", 10.0, 4000),
            WeeklyChallengeEntry("user3", "Maria", 7.0, 3000)
        )

        val userRank = entries.indexOfFirst {
            it.userId == currentUserId
        } + 1

        assertEquals(2, userRank)
    }
}