package com.pelesstefania.runiviva

import com.pelesstefania.runiviva.model.WeeklyChallengeEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyChallengeEntryTest {

    @Test
    fun entriesAreSortedByDistanceDescending() {
        val entries = listOf(
            WeeklyChallengeEntry("1", "Ana", 4.0, 1800),
            WeeklyChallengeEntry("2", "Maria", 10.0, 3600),
            WeeklyChallengeEntry("3", "Ioana", 7.0, 2500)
        )

        val sorted = entries.sortedWith(
            compareByDescending<WeeklyChallengeEntry> {
                it.distanceKm
            }.thenBy {
                it.durationSeconds
            }
        )

        assertEquals("Maria", sorted[0].username)
        assertEquals("Ioana", sorted[1].username)
        assertEquals("Ana", sorted[2].username)
    }

    @Test
    fun entriesWithSameDistanceAreSortedByDurationAscending() {
        val entries = listOf(
            WeeklyChallengeEntry("1", "Ana", 10.0, 4000),
            WeeklyChallengeEntry("2", "Maria", 10.0, 3500)
        )

        val sorted = entries.sortedWith(
            compareByDescending<WeeklyChallengeEntry> {
                it.distanceKm
            }.thenBy {
                it.durationSeconds
            }
        )

        assertEquals("Maria", sorted[0].username)
    }
}