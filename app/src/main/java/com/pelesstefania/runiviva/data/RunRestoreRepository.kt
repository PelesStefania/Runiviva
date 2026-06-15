package com.pelesstefania.runiviva.data

import android.content.Context
import com.pelesstefania.runiviva.model.LocalRunSession

class RunRestoreRepository(context: Context) {

    private val localRunRepository = LocalRunRepository(context)
    private val runRepository = RunRepository()

    suspend fun restoreRunsFromFirebase(userId: String) {
        val firebaseRuns = runRepository.getRunsForUserSuspend(userId)

        for (run in firebaseRuns) {
            val localRun = LocalRunSession(
                runId = run.runId,
                userId = run.userId,
                date = run.date,
                startTime = run.startTime,
                endTime = run.endTime,
                durationSeconds = run.durationSeconds,
                distanceKm = run.distanceKm,
                paceMinPerKm = run.paceMinPerKm,
                isSynced = true
            )

            localRunRepository.saveRunLocally(localRun)
        }
    }
}