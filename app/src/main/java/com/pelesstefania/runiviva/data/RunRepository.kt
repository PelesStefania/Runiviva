package com.pelesstefania.runiviva.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.model.RunSession
import kotlinx.coroutines.tasks.await

class RunRepository {
    private val firestore = FirebaseFirestore.getInstance()


    suspend fun saveRunSuspend(runSession: RunSession) {
        firestore.collection("runs")
            .document(runSession.runId)
            .set(runSession)
            .await()
    }

    suspend fun getRunsForUserSuspend(userId: String): List<RunSession> {
        val snapshot = firestore.collection("runs")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(RunSession::class.java)
        }
    }
}