package com.pelesstefania.runiviva.data

import android.content.Context
import com.pelesstefania.runiviva.local.DatabaseProvider
import com.pelesstefania.runiviva.model.LocalRunSession

class LocalRunRepository(context: Context) {
    private val runDao = DatabaseProvider.getDatabase(context).runDao()

    suspend fun saveRunLocally(run: LocalRunSession) {
        runDao.insertRun(run)
    }


    suspend fun getUnsyncedRuns(): List<LocalRunSession> {
        return runDao.getUnsyncedRuns()
    }

    suspend fun markAsSynced(runId: String) {
        runDao.markAsSynced(runId)
    }

    suspend fun getRunsForUser(userId: String): List<LocalRunSession> {
        return runDao.getRunsForUser(userId)
    }

    suspend fun getRunsForUserByDate(userId: String, date: String): List<LocalRunSession> {
        return runDao.getRunsForUserByDate(userId, date)
    }

    suspend fun getRunDates(userId: String): List<String> {
        return runDao.getRunDates(userId)
    }

    suspend fun getTotalDistanceForUser(userId: String): Double {
        return runDao.getTotalDistanceForUser(userId) ?: 0.0
    }

    suspend fun getTotalRunsForUser(userId: String): Int {
        return runDao.getTotalRunsForUser(userId)
    }

    suspend fun getTotalDurationForDate(userId: String, date: String): Int {
        return runDao.getTotalDurationForDate(userId, date) ?: 0
    }

    suspend fun getTotalDistanceForDate(userId: String, date: String): Double {
        return runDao.getTotalDistanceForDate(userId, date) ?: 0.0
    }

    suspend fun getRunCountForDate(userId: String, date: String): Int {
        return runDao.getRunCountForDate(userId, date)
    }
}