package com.pelesstefania.runiviva.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.R
import com.pelesstefania.runiviva.data.DailySummaryRepository
import com.pelesstefania.runiviva.data.RunRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.model.DailySummary
import com.pelesstefania.runiviva.model.RunSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun HomeScreen() {
    val backgroundColor = Color(0xFFD9F0FF)
    val cardColor = Color(0xFFEAF6FF)

    val context = LocalContext.current
    val runRepository = remember { RunRepository() }
    val userRepository = remember { UserRepository() }
    val dailySummaryRepository = remember { DailySummaryRepository() }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var user by remember { mutableStateOf<AppUser?>(null) }
    var todaySummary by remember { mutableStateOf<DailySummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            if (remainingSeconds > 0) "$minutes min $remainingSeconds sec" else "$minutes min"
        } else {
            "$seconds sec"
        }
    }

    fun loadHomeData() {
        if (currentUser == null) {
            Toast.makeText(context, "No logged in user", Toast.LENGTH_SHORT).show()
            isLoading = false
            return
        }

        isLoading = true

        userRepository.getUserById(
            uid = currentUser.uid,
            onSuccess = { loadedUser ->
                user = loadedUser

                dailySummaryRepository.getDailySummary(
                    userId = currentUser.uid,
                    date = currentDate,
                    onSuccess = { summary ->
                        todaySummary = summary
                        isLoading = false
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        isLoading = false
                    }
                )
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadHomeData()
    }

    val moodMessage = when {
        (todaySummary?.runCount ?: 0) > 0 -> "Nice. You actually moved today."
        else -> "Get up and move."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Spacer(modifier = Modifier.height(80.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...")
            return@Column
        }

        Text(
            text = "Welcome, ${user?.username ?: "Runner"}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.runifericita),
                    contentDescription = "Runi character",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = moodMessage,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                InfoRow(
                    "Distance",
                    String.format(Locale.getDefault(), "%.2f km", todaySummary?.totalDistanceKm ?: 0.0)
                )
                InfoRow("Runs", "${todaySummary?.runCount ?: 0}")
                InfoRow("Duration", formatDuration(todaySummary?.totalDurationSeconds ?: 0))
                InfoRow(
                    "Avg pace",
                    String.format(Locale.getDefault(), "%.2f min/km", todaySummary?.averagePaceMinPerKm ?: 0.0)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Overall",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                InfoRow("Streak", "${user?.streak ?: 0}")
                InfoRow("Total runs", "${user?.totalRuns ?: 0}")
                InfoRow(
                    "Total distance",
                    String.format(Locale.getDefault(), "%.2f km", user?.totalDistanceKm ?: 0.0)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (currentUser == null) {
                    Toast.makeText(context, "No logged in user", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val start = System.currentTimeMillis()
                val end = start + 900_000

                val testRun = RunSession(
                    runId = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    date = currentDate,
                    startTime = start,
                    endTime = end,
                    durationSeconds = 900,
                    distanceKm = 2.4,
                    paceMinPerKm = 6.25,
                    mode = "normal"
                )

                runRepository.saveRun(
                    runSession = testRun,
                    onSuccess = {
                        userRepository.getUserById(
                            uid = currentUser.uid,
                            onSuccess = { loadedUser ->
                                val updatedUser = AppUser(
                                    uid = loadedUser.uid,
                                    username = loadedUser.username,
                                    email = loadedUser.email,
                                    streak = if (loadedUser.lastRunDate == currentDate) {
                                        loadedUser.streak
                                    } else {
                                        loadedUser.streak + 1
                                    },
                                    totalRuns = loadedUser.totalRuns + 1,
                                    totalDistanceKm = loadedUser.totalDistanceKm + testRun.distanceKm,
                                    lastRunDate = currentDate
                                )

                                userRepository.updateUser(
                                    user = updatedUser,
                                    onSuccess = {
                                        dailySummaryRepository.getDailySummary(
                                            userId = currentUser.uid,
                                            date = currentDate,
                                            onSuccess = { existingSummary ->
                                                val totalDistance =
                                                    (existingSummary?.totalDistanceKm ?: 0.0) + testRun.distanceKm
                                                val totalDuration =
                                                    (existingSummary?.totalDurationSeconds ?: 0) + testRun.durationSeconds
                                                val runCount =
                                                    (existingSummary?.runCount ?: 0) + 1

                                                val averagePace =
                                                    if (totalDistance > 0) {
                                                        (totalDuration / 60.0) / totalDistance
                                                    } else {
                                                        0.0
                                                    }

                                                val updatedSummary = DailySummary(
                                                    userId = currentUser.uid,
                                                    date = currentDate,
                                                    totalDistanceKm = totalDistance,
                                                    totalDurationSeconds = totalDuration,
                                                    runCount = runCount,
                                                    averagePaceMinPerKm = averagePace,
                                                    didRun = true
                                                )

                                                dailySummaryRepository.saveDailySummary(
                                                    summary = updatedSummary,
                                                    onSuccess = {
                                                        Toast.makeText(
                                                            context,
                                                            "Test run saved",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        loadHomeData()
                                                    },
                                                    onError = { errorMessage ->
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            },
                                            onError = { errorMessage ->
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    onError = { errorMessage ->
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Save Test Run")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
}