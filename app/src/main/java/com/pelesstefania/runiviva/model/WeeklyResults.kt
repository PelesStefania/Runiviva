package com.pelesstefania.runiviva.model

data class WeeklyResults(
    val top3: List<WeeklyChallengeEntry> = emptyList(),
    val userRank: Int = 0,
    val userDistanceKm: Double = 0.0
)