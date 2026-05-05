package com.pelesstefania.runiviva.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.model.DailySummary

class DailySummaryRepository {
    private val firestore = FirebaseFirestore.getInstance()

    private fun documentId(userId: String, date: String): String {
        return "${userId}_$date"
    }

    fun getDailySummary(
        userId: String,
        date: String,
        onSuccess: (DailySummary?) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("dailySummaries")
            .document(documentId(userId, date))
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document.toObject(DailySummary::class.java))
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load daily summary")
            }
    }

    fun saveDailySummary(
        summary: DailySummary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("dailySummaries")
            .document(documentId(summary.userId, summary.date))
            .set(summary)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save daily summary")
            }
    }
}