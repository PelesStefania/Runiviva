package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.FriendRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.model.FriendRequest
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen() {
    val bgTop = Color(0xFFD9F0FF)
    val bgBottom = Color(0xFFEAF6FF)
    val primary = Color(0xFF4B67A1)
    val cardColor = Color.White
    val softBlue = Color(0xFFF4F8FF)
    val green = Color(0xFF5DBB73)
    val red = Color(0xFFE57373)

    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val userRepository = remember { UserRepository() }
    val friendRepository = remember { FriendRepository() }

    var currentAppUser by remember { mutableStateOf<AppUser?>(null) }
    var searchUsername by remember { mutableStateOf("") }
    var searchedUser by remember { mutableStateOf<AppUser?>(null) }
    var searchMessage by remember { mutableStateOf("") }
    var searchedUserStatus by remember { mutableStateOf("") }

    var incomingRequests by remember {
        mutableStateOf<List<FriendRequest>>(emptyList())
    }

    var requestUsers by remember {
        mutableStateOf<Map<String, AppUser>>(emptyMap())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    fun loadData() {
        if (currentUser == null) {
            isLoading = false
            return
        }

        isLoading = true

        userRepository.getUserById(
            uid = currentUser.uid,
            onSuccess = { loadedUser ->
                currentAppUser = loadedUser

                coroutineScope.launch {
                    try {
                        val requests = friendRepository.getIncomingRequests(currentUser.uid)
                        incomingRequests = requests

                        val loadedRequestUsers = mutableMapOf<String, AppUser>()

                        requests.forEach { request ->
                            userRepository.getUserById(
                                uid = request.senderId,
                                onSuccess = { senderUser ->
                                    loadedRequestUsers[request.senderId] = senderUser
                                    requestUsers = loadedRequestUsers.toMap()
                                },
                                onError = {}
                            )
                        }
                    } catch (e: Exception) {
                        incomingRequests = emptyList()
                        requestUsers = emptyMap()
                    }

                    isLoading = false
                }
            },
            onError = {
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(bgTop, bgBottom)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = primary
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Find a friend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchUsername,
                    onValueChange = {
                        searchUsername = it
                    },
                    label = {
                        Text("Username")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (searchUsername.isBlank()) {
                            searchMessage = "Write a username first."
                            searchedUser = null
                            searchedUserStatus = ""
                            return@Button
                        }

                        userRepository.searchUserByUsername(
                            username = searchUsername,
                            onSuccess = { user ->
                                searchedUser = user

                                if (user == null) {
                                    searchMessage = "No user found."
                                    searchedUserStatus = ""
                                    return@searchUserByUsername
                                }

                                if (currentUser == null) {
                                    searchMessage = "Could not search."
                                    searchedUserStatus = ""
                                    return@searchUserByUsername
                                }

                                coroutineScope.launch {
                                    try {
                                        searchedUserStatus =
                                            when {
                                                user.uid == currentUser.uid -> "This is you"

                                                friendRepository.areFriends(
                                                    currentUser.uid,
                                                    user.uid
                                                ) -> "Friends"

                                                friendRepository.hasPendingRequest(
                                                    currentUser.uid,
                                                    user.uid
                                                ) -> "Pending"

                                                else -> "Add"
                                            }

                                        searchMessage = ""
                                    } catch (e: Exception) {
                                        searchedUserStatus = ""
                                        searchMessage = "Could not check status."
                                    }
                                }
                            },
                            onError = { error ->
                                searchedUser = null
                                searchedUserStatus = ""
                                searchMessage = error
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Search")
                }

                if (searchMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = searchMessage,
                        color = primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (searchedUser != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = softBlue)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserInitialCircle(
                                    username = searchedUser!!.username,
                                    primary = primary
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = searchedUser!!.username,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primary
                                    )

                                    Text(
                                        text = "Runner",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = primary.copy(alpha = 0.75f)
                                    )
                                }
                            }

                            if (searchedUserStatus == "Add") {
                                Button(
                                    onClick = {
                                        val sender = currentAppUser
                                        val receiver = searchedUser

                                        if (sender == null || receiver == null) {
                                            searchMessage = "Could not send request."
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            try {
                                                friendRepository.sendFriendRequest(
                                                    sender = sender,
                                                    receiver = receiver
                                                )

                                                searchedUserStatus = "Pending"
                                                searchMessage = "Friend request sent."
                                            } catch (e: Exception) {
                                                searchMessage =
                                                    e.message ?: "Could not send request."
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Add")
                                }
                            } else {
                                Text(
                                    text = searchedUserStatus,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = primary)
                } else if (incomingRequests.isEmpty()) {
                    Text(
                        text = "No requests right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = primary.copy(alpha = 0.75f)
                    )
                } else {
                    incomingRequests.forEach { request ->
                        val senderUser = requestUsers[request.senderId]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = softBlue)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserInitialCircle(
                                    username = senderUser?.username ?: request.senderUsername,
                                    primary = primary
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = senderUser?.username ?: request.senderUsername,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    friendRepository.acceptFriendRequest(request)
                                                    loadData()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = green
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text("Accept")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    friendRepository.declineFriendRequest(
                                                        request.requestId
                                                    )
                                                    loadData()
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = "Decline",
                                                color = red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UserInitialCircle(
    username: String,
    primary: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(primary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = primary
        )
    }
}