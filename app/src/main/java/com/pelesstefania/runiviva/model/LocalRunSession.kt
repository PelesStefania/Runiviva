package com.pelesstefania.runiviva.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_runs")
data class LocalRunSession(
    @PrimaryKey
    val runId: String,
    val userId: String,
    val date: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val distanceKm: Double,
    val paceMinPerKm: Double,
    val mode: String,
    val isSynced: Boolean = false
)