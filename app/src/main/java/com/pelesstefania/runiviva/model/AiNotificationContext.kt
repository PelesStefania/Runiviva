package com.pelesstefania.runiviva.model

data class AiNotificationContext(

    val username: String,

    val notificationTone: String,

    val ranToday: Boolean,

    val todayDistanceKm: Double,

    val totalDistanceKm: Double,

    val friendRunsLast7Days: Int,

    val hasFriends: Boolean
)