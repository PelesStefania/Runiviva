package com.pelesstefania.runiviva.model

data class AppUser(
    val uid: String = "",
    val username: String = "",
    val usernameLowercase: String = "",
    val email: String = "",
    val totalRuns: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val lastRunDate: String = "",
    val notificationTone: String = "funny",
    val notificationsEnabled: Boolean = true
)