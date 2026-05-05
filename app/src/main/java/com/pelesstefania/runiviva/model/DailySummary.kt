package com.pelesstefania.runiviva.model

data class DailySummary(
    val userId: String = "",
    val date: String = "",
    val totalDistanceKm: Double = 0.0,
    val totalDurationSeconds: Int = 0,
    val runCount: Int = 0,
    val averagePaceMinPerKm: Double = 0.0,
    val didRun: Boolean = false
)