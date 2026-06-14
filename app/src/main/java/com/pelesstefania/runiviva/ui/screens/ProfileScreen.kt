package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.AiNotificationRepository
import com.pelesstefania.runiviva.data.AiService
import com.pelesstefania.runiviva.data.FriendRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val backgroundColor = Color(0xFFD9F0FF)
    val primary = Color(0xFF4B67A1)
    val darkBlue = Color(0xFF2F3E75)
    val cardColor = Color.White
    val softBlue = Color(0xFFF4F8FF)

    val currentUser = FirebaseAuth.getInstance().currentUser

    val userRepository = remember { UserRepository() }
    val friendRepository = remember { FriendRepository() }
    val aiService = remember { AiService() }
    val aiNotificationRepository = remember {
        AiNotificationRepository(context)
    }

    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<AppUser?>(null) }
    var friends by remember { mutableStateOf<List<AppUser>>(emptyList()) }

    var toneMenuExpanded by remember { mutableStateOf(false) }

    var aiMessage by remember { mutableStateOf("") }
    var isGeneratingAiMessage by remember { mutableStateOf(false) }

    val toneOptions = listOf(
        "encouraging" to "Encouraging",
        "funny" to "Funny",
        "rude" to "Rude",
        "competitive" to "Competitive"
    )

    LaunchedEffect(Unit) {
        if (currentUser == null) return@LaunchedEffect

        userRepository.getUserById(
            uid = currentUser.uid,
            onSuccess = { loadedUser ->
                user = loadedUser
            },
            onError = {}
        )

        coroutineScope.launch {
            friends = friendRepository.getFriendUsers(currentUser.uid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InitialCircleLarge(
                    username = user?.username ?: "Runner",
                    primary = primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.username ?: "Runner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = darkBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${friends.size} friends",
                    style = MaterialTheme.typography.bodyLarge,
                    color = primary,
                    fontWeight = FontWeight.SemiBold
                )
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
                    text = "Friends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (friends.isEmpty()) {
                    Text(
                        text = "No friends yet.",
                        color = primary.copy(alpha = 0.75f)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(friends) { friend ->
                            FriendProfileItem(
                                friend = friend,
                                primary = primary,
                                softBlue = softBlue,
                                onClick = {
                                    navController.navigate("${Routes.FRIEND_DETAILS}/${friend.uid}")
                                }
                            )
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
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = toneMenuExpanded,
                    onExpandedChange = {
                        toneMenuExpanded = !toneMenuExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = toneOptions.firstOrNull {
                            it.first == (user?.notificationTone ?: "funny")
                        }?.second ?: "Funny",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Notification tone")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = toneMenuExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = toneMenuExpanded,
                        onDismissRequest = {
                            toneMenuExpanded = false
                        }
                    ) {
                        toneOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(option.second)
                                },
                                onClick = {
                                    val current = user ?: return@DropdownMenuItem

                                    val updatedUser = current.copy(
                                        notificationTone = option.first
                                    )

                                    user = updatedUser
                                    toneMenuExpanded = false

                                    userRepository.updateUser(
                                        user = updatedUser,
                                        onSuccess = {},
                                        onError = {}
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (user?.notificationTone ?: "funny") {
                        "encouraging" -> "Messages will sound supportive and calm."
                        "rude" -> "Messages will be harsher and more direct."
                        "competitive" -> "Messages will push you with comparison and challenge."
                        else -> "Messages will be casual and funny."
                    },
                    color = primary.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // TEST
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
                    text = "AI test",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isGeneratingAiMessage = true
                            aiMessage = ""

                            try {
                                val current = user ?: return@launch

                                val aiContext =
                                    aiNotificationRepository
                                        .buildContext(current)

                                aiMessage =
                                    aiService
                                        .generateNotification(aiContext)
                                        .message
                            } catch (e: Exception) {
                                aiMessage = e.message ?: "AI error."
                            }

                            isGeneratingAiMessage = false
                        }
                    },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (isGeneratingAiMessage) {
                            "Generating..."
                        } else {
                            "Test AI"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (aiMessage.isNotBlank()) {
                    Text(
                        text = aiMessage,
                        color = darkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        // TEST

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()

                navController.navigate(Routes.LOGIN) {
                    popUpTo(0)
                }
            },
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InitialCircleLarge(
    username: String,
    primary: Color
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(primary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = username.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = primary
        )
    }
}

@Composable
fun InitialCircleSmall(
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

@Composable
fun FriendProfileItem(
    friend: AppUser,
    primary: Color,
    softBlue: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = softBlue
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InitialCircleSmall(
                username = friend.username,
                primary = primary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = friend.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = primary
            )
        }
    }
}