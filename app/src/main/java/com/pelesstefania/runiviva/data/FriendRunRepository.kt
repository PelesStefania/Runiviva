package com.pelesstefania.runiviva.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.model.LocalRunSession
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class FriendRunRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getFriendRunsLast7Days(
        friendId: String
    ): List<LocalRunSession> {
        val startDate = LocalDate.now()
            .minusDays(6)
            .toString()

        val snapshot = firestore.collection("runs")
            .whereEqualTo("userId", friendId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            val date = document.getString("date") ?: return@mapNotNull null

            if (date < startDate) return@mapNotNull null

            LocalRunSession(
                runId = document.getString("runId") ?: document.id,
                userId = document.getString("userId") ?: "",
                date = date,
                startTime = document.getLong("startTime") ?: 0L,
                endTime = document.getLong("endTime") ?: 0L,
                durationSeconds = document.getLong("durationSeconds")?.toInt() ?: 0,
                distanceKm = document.getDouble("distanceKm") ?: 0.0,
                paceMinPerKm = document.getDouble("paceMinPerKm") ?: 0.0,
                isSynced = true
            )
        }.sortedByDescending {
            it.startTime
        }
    }
}