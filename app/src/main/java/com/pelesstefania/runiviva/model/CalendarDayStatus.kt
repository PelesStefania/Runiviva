package com.pelesstefania.runiviva.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_day_statuses")
data class CalendarDayStatus(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val status: String = "",
    val isSynced: Boolean = false
)