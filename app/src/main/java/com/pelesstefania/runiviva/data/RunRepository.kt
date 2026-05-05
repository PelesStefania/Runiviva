package com.pelesstefania.runiviva.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.model.RunSession

class RunRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveRun(
        runSession: RunSession,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("runs")
            .document(runSession.runId)
            .set(runSession)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save run")
            }
    }
}