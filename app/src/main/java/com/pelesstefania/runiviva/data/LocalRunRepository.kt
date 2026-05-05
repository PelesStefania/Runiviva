package com.pelesstefania.runiviva.data

import android.content.Context
import com.pelesstefania.runiviva.local.DatabaseProvider
import com.pelesstefania.runiviva.model.LocalRunSession

class LocalRunRepository(context: Context) {
    private val runDao = DatabaseProvider.getDatabase(context).runDao()

    suspend fun saveRunLocally(run: LocalRunSession) {
        runDao.insertRun(run)
    }

    suspend fun getAllRuns(): List<LocalRunSession> {
        return runDao.getAllRuns()
    }

    suspend fun getUnsyncedRuns(): List<LocalRunSession> {
        return runDao.getUnsyncedRuns()
    }

    suspend fun markAsSynced(runId: String) {
        runDao.markAsSynced(runId)
    }
}