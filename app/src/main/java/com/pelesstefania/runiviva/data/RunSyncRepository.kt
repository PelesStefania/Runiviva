package com.pelesstefania.runiviva.data

import android.content.Context
import com.pelesstefania.runiviva.model.RunSession

class RunSyncRepository(context: Context) {

    private val localRunRepository = LocalRunRepository(context)
    private val runRepository = RunRepository()

    suspend fun syncUnsyncedRuns() {
        val unsyncedRuns = localRunRepository.getUnsyncedRuns()

        for (localRun in unsyncedRuns) {
            val runSession = RunSession(
                runId = localRun.runId,
                userId = localRun.userId,
                date = localRun.date,
                startTime = localRun.startTime,
                endTime = localRun.endTime,
                durationSeconds = localRun.durationSeconds,
                distanceKm = localRun.distanceKm,
                paceMinPerKm = localRun.paceMinPerKm
            )

            runRepository.saveRunSuspend(runSession)

            localRunRepository.markAsSynced(localRun.runId)
        }
    }
}