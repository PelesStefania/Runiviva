package com.pelesstefania.runiviva.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.local.DatabaseProvider
import com.pelesstefania.runiviva.model.CalendarDayStatus
import kotlinx.coroutines.tasks.await

class CalendarStatusRepository(context: Context) {

    private val calendarStatusDao =
        DatabaseProvider.getDatabase(context).calendarStatusDao()

    private val firestore = FirebaseFirestore.getInstance()

    private fun statusId(userId: String, date: String): String {
        return "${userId}_$date"
    }

    suspend fun getStatusesForUser(userId: String): List<CalendarDayStatus> {
        return calendarStatusDao.getStatusesForUser(userId)
    }

    suspend fun markSickDay(userId: String, date: String) {
        val status = CalendarDayStatus(
            id = statusId(userId, date),
            userId = userId,
            date = date,
            status = "sick",
            isSynced = false
        )

        calendarStatusDao.insertStatus(status)

        try {
            saveStatusToFirebase(status)
            calendarStatusDao.markAsSynced(status.id)
        } catch (e: Exception) {
        }
    }

    suspend fun removeSickDay(userId: String, date: String) {
        val id = statusId(userId, date)

        calendarStatusDao.deleteStatus(id)

        try {
            firestore.collection("calendarStatuses")
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
        }
    }

    suspend fun syncUnsyncedStatuses(userId: String) {
        val unsyncedStatuses = calendarStatusDao.getUnsyncedStatuses(userId)

        for (status in unsyncedStatuses) {
            saveStatusToFirebase(status)
            calendarStatusDao.markAsSynced(status.id)
        }
    }

    suspend fun restoreStatusesFromFirebase(userId: String) {
        val snapshot = firestore.collection("calendarStatuses")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val statuses = snapshot.documents.mapNotNull {
            it.toObject(CalendarDayStatus::class.java)
        }

        for (status in statuses) {
            calendarStatusDao.insertStatus(
                status.copy(isSynced = true)
            )
        }
    }

    private suspend fun saveStatusToFirebase(status: CalendarDayStatus) {
        firestore.collection("calendarStatuses")
            .document(status.id)
            .set(status.copy(isSynced = true))
            .await()
    }
}