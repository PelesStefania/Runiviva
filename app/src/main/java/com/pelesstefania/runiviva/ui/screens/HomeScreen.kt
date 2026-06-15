package com.pelesstefania.runiviva.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.R
import com.pelesstefania.runiviva.data.LocalRunRepository
import com.pelesstefania.runiviva.data.RunRestoreRepository
import com.pelesstefania.runiviva.data.RunSyncRepository
import com.pelesstefania.runiviva.data.UserRepository
import com.pelesstefania.runiviva.model.AppUser
import com.pelesstefania.runiviva.navigation.Routes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import com.pelesstefania.runiviva.data.WeeklyChallengeRepository
import com.pelesstefania.runiviva.model.WeeklyResults
import java.time.temporal.WeekFields

@Composable
fun HomeScreen(navController: NavController) {
    val backgroundColor = Color(0xFFD9F0FF)
    val cardColor = Color(0xFFEAF6FF)
    val primaryColor = Color(0xFF4B67A1)
    val darkBlue = Color(0xFF2F3E75)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val localRunRepository = remember { LocalRunRepository(context) }
    val runSyncRepository = remember { RunSyncRepository(context) }
    val runRestoreRepository = remember { RunRestoreRepository(context) }
    val userRepository = remember { UserRepository() }

    val weeklyChallengeRepository = remember {
        WeeklyChallengeRepository()
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    val currentDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var user by remember { mutableStateOf<AppUser?>(null) }
    var showWeeklyResults by remember {
        mutableStateOf(false)
    }

    var weeklyResults by remember {
        mutableStateOf<WeeklyResults?>(null)
    }
    var isLoading by remember { mutableStateOf(true) }

    var todayDistance by remember { mutableStateOf(0.0) }
    var todayDuration by remember { mutableStateOf(0) }
    var todayRunCount by remember { mutableStateOf(0) }

    var totalRuns by remember { mutableStateOf(0) }
    var totalDistance by remember { mutableStateOf(0.0) }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return if (minutes > 0) {
            if (remainingSeconds > 0) "$minutes min $remainingSeconds sec" else "$minutes min"
        } else {
            "$seconds sec"
        }
    }


    fun loadLocalStats(userId: String) {
        coroutineScope.launch {
            todayDistance = localRunRepository.getTotalDistanceForDate(
                userId = userId,
                date = currentDate
            )

            todayDuration = localRunRepository.getTotalDurationForDate(
                userId = userId,
                date = currentDate
            )

            todayRunCount = localRunRepository.getRunCountForDate(
                userId = userId,
                date = currentDate
            )

            totalRuns = localRunRepository.getTotalRunsForUser(userId)

            totalDistance = localRunRepository.getTotalDistanceForUser(userId)

            isLoading = false
        }
    }

    fun loadHomeData() {
        if (currentUser == null) {
            isLoading = false
            return
        }

        isLoading = true

        loadLocalStats(currentUser.uid)

        coroutineScope.launch {
            try {
                runSyncRepository.syncUnsyncedRuns()
                runRestoreRepository.restoreRunsFromFirebase(currentUser.uid)
                loadLocalStats(currentUser.uid)
            } catch (e: Exception) {
                loadLocalStats(currentUser.uid)
            }
        }

        userRepository.getUserById(
            uid = currentUser.uid,
            onSuccess = { loadedUser ->
                user = loadedUser
            },
            onError = {
                user = null
            }
        )
    }

    fun checkWeeklyResults() {

        if (currentUser == null) return

        coroutineScope.launch {

            val prefs = context.getSharedPreferences(
                "weekly_results",
                android.content.Context.MODE_PRIVATE
            )

            val currentWeek =
                LocalDate.now().get(
                    WeekFields.ISO.weekOfWeekBasedYear()
                )

            val lastSeenWeek =
                prefs.getInt("last_seen_week", -1)

            if (lastSeenWeek == currentWeek) {
                return@launch
            }

            weeklyResults =
                weeklyChallengeRepository
                    .getPreviousWeekResults(currentUser.uid)

            showWeeklyResults = true
        }
    }

    LaunchedEffect(Unit) {
        loadHomeData()
        checkWeeklyResults()
    }

    val averagePace =
        if (todayDistance > 0) {
            (todayDuration / 60.0) / todayDistance
        } else {
            0.0
        }

    val moodMessage = when {
        todayRunCount > 0 -> "Nice. You actually moved today."
        else -> "Get up and move."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                CircularProgressIndicator(color = primaryColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...")
                return@Column
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryColor
                    )

                    Text(
                        text = user?.username ?: "Runner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = darkBlue
                    )
                }

            }

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
                fontWeight = FontWeight.SemiBold,
                color = darkBlue
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
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    InfoRow(
                        "Distance",
                        String.format(Locale.getDefault(), "%.2f km", todayDistance)
                    )

                    InfoRow("Runs", "$todayRunCount")

                    InfoRow(
                        "Duration",
                        formatDuration(todayDuration)
                    )

                    InfoRow(
                        "Avg pace",
                        String.format(Locale.getDefault(), "%.2f min/km", averagePace)
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
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    InfoRow("Total runs", "$totalRuns")

                    InfoRow(
                        "Total distance",
                        String.format(Locale.getDefault(), "%.2f km", totalDistance)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        if (
            showWeeklyResults &&
            weeklyResults != null
        ) {

            WeeklyResultsOverlay(
                results = weeklyResults!!,
                onClose = {

                    val prefs = context.getSharedPreferences(
                        "weekly_results",
                        android.content.Context.MODE_PRIVATE
                    )

                    val currentWeek =
                        LocalDate.now().get(
                            WeekFields.ISO.weekOfWeekBasedYear()
                        )

                    prefs.edit()
                        .putInt(
                            "last_seen_week",
                            currentWeek
                        )
                        .apply()

                    showWeeklyResults = false
                }
            )
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