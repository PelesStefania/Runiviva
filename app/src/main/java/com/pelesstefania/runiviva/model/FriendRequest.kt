package com.pelesstefania.runiviva.model

data class FriendRequest(
    val requestId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val receiverId: String = "",
    val status: String = "pending"
)