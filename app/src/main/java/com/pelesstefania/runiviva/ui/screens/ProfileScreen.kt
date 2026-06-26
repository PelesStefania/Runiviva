package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.pelesstefania.runiviva.data.FriendRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.navigation.Routes
import com.pelesstefania.runiviva.notifications.NotificationScheduler
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.width
import com.pelesstefania.runiviva.data.CalendarStatusRepository
import java.time.LocalDate
import com.pelesstefania.runiviva.data.LocalRunRepository
import com.pelesstefania.runiviva.data.AiNotificationRepository
import com.pelesstefania.runiviva.data.AiService

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
    val notificationScheduler = remember { NotificationScheduler(context) }

    val calendarStatusRepository = remember {
        CalendarStatusRepository(context)
    }

    val aiNotificationRepository = remember {
        AiNotificationRepository(context)
    }

    val aiService = remember {
        AiService()
    }

    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<AppUser?>(null) }
    var friends by remember { mutableStateOf<List<AppUser>>(emptyList()) }

    var toneMenuExpanded by remember { mutableStateOf(false) }

    var previewMessage by remember {
        mutableStateOf<String?>(null)
    }

    val toneOptions = listOf(
        "encouraging" to "Encouraging",
        "funny" to "Funny",
        "rude" to "Rude",
        "competitive" to "Competitive",
        "injury" to "Injury / Recovery"

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
        )
        {
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        friends.forEach { friend ->
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
                                        onSuccess = {
                                            if (option.first == "injury") {
                                                coroutineScope.launch {
                                                    val today = LocalDate.now().toString()

                                                    val runCountToday = LocalRunRepository(context).getRunCountForDate(
                                                        userId = current.uid,
                                                        date = today
                                                    )

                                                    if (runCountToday == 0) {
                                                        calendarStatusRepository.markSickDay(
                                                            userId = current.uid,
                                                            date = today
                                                        )
                                                    }
                                                }
                                            }
                                        },
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
                        "injury" -> "Messages will remind you to rest and protect your health."
                        else -> "Messages will be casual and funny."
                    },
                    color = primary.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Enable Notifications Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = darkBlue
                    )

                    Switch(
                        checked = user?.notificationsEnabled ?: true,
                        onCheckedChange = { isEnabled ->
                            val current = user ?: return@Switch

                            val updatedUser = current.copy(
                                notificationsEnabled = isEnabled
                            )

                            user = updatedUser

                            userRepository.updateUser(
                                user = updatedUser,
                                onSuccess = {
                                    notificationScheduler.scheduleNotifications(isEnabled)
                                },
                                onError = {}
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primary,
                            checkedTrackColor = primary.copy(alpha = 0.3f)
                        )
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val current = user ?: return@Button

                        coroutineScope.launch {
                            try {
                                val aiContext =
                                    aiNotificationRepository.buildContext(current)

                                val aiMessage =
                                    aiService.generateNotification(aiContext)

                                previewMessage = aiMessage.message
                            } catch (e: Exception) {
                                previewMessage = "Could not generate preview."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Preview AI notification")
                }

                previewMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = softBlue
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = message,
                                color = darkBlue
                            )
                        }
                    }
                }
            }
        }

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialCircleSmall(
                username = friend.username,
                primary = primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = friend.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Check their runs",
                    style = MaterialTheme.typography.bodySmall,
                    color = primary.copy(alpha = 0.70f)
                )
            }
        }
    }
}