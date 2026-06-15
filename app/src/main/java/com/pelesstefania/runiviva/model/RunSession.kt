package com.pelesstefania.runiviva.model

data class RunSession(
    val runId: String = "",
    val userId: String = "",
    val date: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val durationSeconds: Int = 0,
    val distanceKm: Double = 0.0,
    val paceMinPerKm: Double = 0.0
)