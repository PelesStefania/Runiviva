package com.pelesstefania.runiviva.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.model.FriendRequest
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FriendRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun sendFriendRequest(
        sender: AppUser,
        receiver: AppUser
    ) {
        if (sender.uid == receiver.uid) return

        if (areFriends(sender.uid, receiver.uid)) return

        if (hasPendingRequest(sender.uid, receiver.uid)) return

        val request = FriendRequest(
            requestId = UUID.randomUUID().toString(),
            senderId = sender.uid,
            senderUsername = sender.username,
            receiverId = receiver.uid,
            status = "pending"
        )

        firestore.collection("friendRequests")
            .document(request.requestId)
            .set(request)
            .await()
    }

    suspend fun hasPendingRequest(
        senderId: String,
        receiverId: String
    ): Boolean {
        val sent = firestore.collection("friendRequests")
            .whereEqualTo("senderId", senderId)
            .whereEqualTo("receiverId", receiverId)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        if (!sent.isEmpty) return true

        val received = firestore.collection("friendRequests")
            .whereEqualTo("senderId", receiverId)
            .whereEqualTo("receiverId", senderId)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        return !received.isEmpty
    }

    suspend fun areFriends(
        userId: String,
        otherUserId: String
    ): Boolean {
        val document = firestore.collection("friends")
            .document(userId)
            .collection("userFriends")
            .document(otherUserId)
            .get()
            .await()

        return document.exists()
    }

    suspend fun getIncomingRequests(
        userId: String
    ): List<FriendRequest> {
        val snapshot = firestore.collection("friendRequests")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(FriendRequest::class.java)
        }
    }

    suspend fun acceptFriendRequest(
        request: FriendRequest
    ) {
        firestore.collection("friendRequests")
            .document(request.requestId)
            .update("status", "accepted")
            .await()

        firestore.collection("friends")
            .document(request.senderId)
            .collection("userFriends")
            .document(request.receiverId)
            .set(mapOf("uid" to request.receiverId))
            .await()

        firestore.collection("friends")
            .document(request.receiverId)
            .collection("userFriends")
            .document(request.senderId)
            .set(mapOf("uid" to request.senderId))
            .await()
    }

    suspend fun declineFriendRequest(
        requestId: String
    ) {
        firestore.collection("friendRequests")
            .document(requestId)
            .delete()
            .await()
    }

    suspend fun getFriends(
        userId: String
    ): List<String> {
        val snapshot = firestore.collection("friends")
            .document(userId)
            .collection("userFriends")
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.getString("uid")
        }
    }

    suspend fun getFriendUsers(
        userId: String
    ): List<AppUser> {
        val friendIds = getFriends(userId)

        val friends = mutableListOf<AppUser>()

        for (friendId in friendIds) {
            val document = firestore.collection("users")
                .document(friendId)
                .get()
                .await()

            val friend = document.toObject(AppUser::class.java)

            if (friend != null) {
                friends.add(friend)
            }
        }

        return friends
    }
}