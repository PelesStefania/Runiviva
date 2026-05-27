package com.pelesstefania.runiviva.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pelesstefania.runiviva.model.CalendarDayStatus

@Dao
interface CalendarStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: CalendarDayStatus)

    @Query("DELETE FROM calendar_day_statuses WHERE id = :id")
    suspend fun deleteStatus(id: String)

    @Query("SELECT * FROM calendar_day_statuses WHERE userId = :userId")
    suspend fun getStatusesForUser(userId: String): List<CalendarDayStatus>

    @Query("SELECT * FROM calendar_day_statuses WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedStatuses(userId: String): List<CalendarDayStatus>

    @Query("UPDATE calendar_day_statuses SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}