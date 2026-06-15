package com.pelesstefania.runiviva.model

data class WeeklyChallengeEntry(
    val userId: String = "",
    val username: String = "",
    val distanceKm: Double = 0.0,
    val durationSeconds: Int = 0
)