package com.pelesstefania.runiviva.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.model.AppUser

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveUser(
        user: AppUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to save user")
            }
    }

    fun getUserById(
        uid: String,
        onSuccess: (AppUser) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(AppUser::class.java)
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError("User not found")
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to load user")
            }
    }

    fun updateUser(
        user: AppUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("users")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Failed to update user")
            }
    }




}