package com.pelesstefania.runiviva.model

data class AppUser(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val streak: Int = 0,
    val totalRuns: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val lastRunDate: String = ""
)