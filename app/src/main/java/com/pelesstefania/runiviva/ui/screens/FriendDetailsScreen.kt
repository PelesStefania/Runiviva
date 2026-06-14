package com.pelesstefania.runiviva.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pelesstefania.runiviva.data.FriendRunRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.model.LocalRunSession
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun FriendDetailsScreen(
    navController: NavController,
    friendId: String
) {
    val bgTop = Color(0xFFD9F0FF)
    val bgBottom = Color(0xFFEAF6FF)
    val primary = Color(0xFF4B67A1)
    val darkBlue = Color(0xFF2F3E75)
    val cardColor = Color.White
    val softBlue = Color(0xFFF4F8FF)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userRepository = remember { UserRepository() }
    val friendRunRepository = remember { FriendRunRepository() }

    var friend by remember { mutableStateOf<AppUser?>(null) }
    var runs by remember { mutableStateOf<List<LocalRunSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(!isInternetAvailable(context)) }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return if (minutes > 0) {
            if (remainingSeconds > 0) "$minutes min $remainingSeconds sec" else "$minutes min"
        } else {
            "$seconds sec"
        }
    }

    LaunchedEffect(friendId) {
        isOffline = !isInternetAvailable(context)

        if (isOffline) {
            isLoading = false
            return@LaunchedEffect
        }

        userRepository.getUserById(
            uid = friendId,
            onSuccess = { loadedFriend ->
                friend = loadedFriend
            },
            onError = {}
        )

        coroutineScope.launch {
            try {
                runs = friendRunRepository.getFriendRunsLast7Days(friendId)
            } catch (e: Exception) {
                runs = emptyList()
            }

            isLoading = false
        }
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
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = primary
                )
            }

            Text(
                text = "Friend profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = primary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (isOffline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "You're offline",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Friends are unavailable right now. For now, you compete with yourself.",
                        color = primary.copy(alpha = 0.75f)
                    )
                }
            }

            return@Column
        }

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
                    username = friend?.username ?: "?",
                    primary = primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = friend?.username ?: "Loading...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = darkBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Runs from the last 7 days",
                    color = primary.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primary)
            }
        } else if (runs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Text(
                    text = "No runs in the last 7 days.",
                    modifier = Modifier.padding(20.dp),
                    color = primary
                )
            }
        } else {
            runs.forEachIndexed { index, run ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Run #${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = run.date,
                            color = darkBlue
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = softBlue)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                FriendRunRow(
                                    label = "Distance",
                                    value = String.format(
                                        Locale.getDefault(),
                                        "%.2f km",
                                        run.distanceKm
                                    )
                                )

                                FriendRunRow(
                                    label = "Duration",
                                    value = formatDuration(run.durationSeconds)
                                )

                                FriendRunRow(
                                    label = "Average pace",
                                    value = String.format(
                                        Locale.getDefault(),
                                        "%.2f min/km",
                                        run.paceMinPerKm
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRunRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)

        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false

    val capabilities =
        connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}