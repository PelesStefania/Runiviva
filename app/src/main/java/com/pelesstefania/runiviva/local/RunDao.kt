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
}