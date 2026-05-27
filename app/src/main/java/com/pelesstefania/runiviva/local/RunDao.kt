package com.pelesstefania.runiviva.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pelesstefania.runiviva.model.LocalRunSession

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: LocalRunSession)

    @Query("SELECT * FROM local_runs ORDER BY startTime DESC")
    suspend fun getAllRuns(): List<LocalRunSession>

    @Query("SELECT * FROM local_runs WHERE isSynced = 0")
    suspend fun getUnsyncedRuns(): List<LocalRunSession>

    @Query("UPDATE local_runs SET isSynced = 1 WHERE runId = :runId")
    suspend fun markAsSynced(runId: String)

    @Query("SELECT * FROM local_runs WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getRunsForUser(userId: String): List<LocalRunSession>

    @Query("SELECT * FROM local_runs WHERE userId = :userId AND date = :date ORDER BY startTime DESC")
    suspend fun getRunsForUserByDate(userId: String, date: String): List<LocalRunSession>

    @Query("SELECT SUM(distanceKm) FROM local_runs WHERE userId = :userId")
    suspend fun getTotalDistanceForUser(userId: String): Double?

    @Query("SELECT COUNT(*) FROM local_runs WHERE userId = :userId")
    suspend fun getTotalRunsForUser(userId: String): Int

    @Query("SELECT SUM(durationSeconds) FROM local_runs WHERE userId = :userId AND date = :date")
    suspend fun getTotalDurationForDate(userId: String, date: String): Int?

    @Query("SELECT SUM(distanceKm) FROM local_runs WHERE userId = :userId AND date = :date")
    suspend fun getTotalDistanceForDate(userId: String, date: String): Double?

    @Query("SELECT COUNT(*) FROM local_runs WHERE userId = :userId AND date = :date")
    suspend fun getRunCountForDate(userId: String, date: String): Int

    @Query("""
    SELECT DISTINCT date 
    FROM local_runs 
    WHERE userId = :userId 
    ORDER BY date DESC
    """)
    suspend fun getRunDates(userId: String): List<String>

}