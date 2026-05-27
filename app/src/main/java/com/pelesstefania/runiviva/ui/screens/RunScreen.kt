package com.pelesstefania.runiviva.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.LocalRunRepository
import com.pelesstefania.runiviva.data.RunSyncRepository
import com.pelesstefania.runiviva.model.LocalRunSession
import com.pelesstefania.runiviva.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun RunScreen(navController: NavController) {
    val context = LocalContext.current

    val bgTop = Color(0xFFD9F0FF)
    val bgBottom = Color(0xFFEAF6FF)
    val primary = Color(0xFF4B67A1)
    val green = Color(0xFF5DBB73)
    val red = Color(0xFFE57373)
    val card = Color(0xFFFFFFFF)
    val softBlue = Color(0xFFBFD7F2)
    val textSoft = Color(0xFF6B5CA5)
    val paleGreen = Color(0xFFE8FFE9)

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val localRunRepository = remember { LocalRunRepository(context) }
    val runSyncRepository = remember { RunSyncRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var distanceMeters by remember { mutableStateOf(0.0) }
    var runStartTimeMillis by remember { mutableLongStateOf(0L) }

    var lastSavedDistanceKm by remember { mutableStateOf<Double?>(null) }
    var lastSavedDurationSeconds by remember { mutableStateOf<Int?>(null) }
    var lastSavedPace by remember { mutableStateOf<Double?>(null) }

    val showSavedOverlay =
        lastSavedDistanceKm != null &&
                lastSavedDurationSeconds != null &&
                lastSavedPace != null

    val locationPoints = remember { mutableStateListOf<Location>() }

    fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            if (remainingSeconds > 0) "$minutes min $remainingSeconds sec" else "$minutes min"
        } else {
            "$seconds sec"
        }
    }

    fun formatDistance(meters: Double): String {
        return String.format(Locale.getDefault(), "%.2f km", meters / 1000.0)
    }

    fun calculatePaceValue(seconds: Long, meters: Double): Double {
        if (meters <= 0.0) return 0.0
        return (seconds / 60.0) / (meters / 1000.0)
    }

    fun formatPace(seconds: Long, meters: Double): String {
        return String.format(Locale.getDefault(), "%.2f min/km", calculatePaceValue(seconds, meters))
    }

    fun resetRunState() {
        elapsedSeconds = 0L
        distanceMeters = 0.0
        locationPoints.clear()
        runStartTimeMillis = 0L
        isRunning = false
        isPaused = false
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isRunning || isPaused) return

                for (newLocation in result.locations) {
                    if (locationPoints.isNotEmpty()) {
                        val lastLocation = locationPoints.last()
                        val results = FloatArray(1)

                        Location.distanceBetween(
                            lastLocation.latitude,
                            lastLocation.longitude,
                            newLocation.latitude,
                            newLocation.longitude,
                            results
                        )

                        val segmentDistance = results[0].toDouble()

                        if (segmentDistance > 1.5) {
                            distanceMeters += segmentDistance
                        }
                    }

                    locationPoints.add(newLocation)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).setMinUpdateDistanceMeters(2f).build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            context.mainLooper
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    LaunchedEffect(isRunning, isPaused) {
        while (isRunning && !isPaused) {
            delay(1000)
            elapsedSeconds++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopLocationUpdates()
        }
    }

    val titleText = when {
        isRunning && !isPaused -> "You’re moving."
        isRunning && isPaused -> "Paused. Dramatic."
        else -> "Ready?"
    }

    val subtitleText = when {
        isRunning && !isPaused -> "Distance is being collected. No pressure. Kind of."
        isRunning && isPaused -> "Taking a break already? Fine."
        else -> "Start the run when you’re done negotiating with yourself."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(bgTop, bgBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
                .alpha(if (showSavedOverlay) 0.38f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Run",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = card),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSoft
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color.White,
                                        bgTop,
                                        softBlue
                                    )
                                )
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.82f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatTime(elapsedSeconds),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = when {
                                        isRunning && !isPaused -> "LIVE"
                                        isRunning && isPaused -> "PAUSED"
                                        else -> "TIMER"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FunMetricCard(
                            label = "Distance",
                            value = formatDistance(distanceMeters),
                            color = softBlue,
                            modifier = Modifier.weight(1f)
                        )

                        FunMetricCard(
                            label = "Pace",
                            value = formatPace(elapsedSeconds, distanceMeters),
                            color = green,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (!isRunning) {
                Button(
                    onClick = {
                        if (!hasLocationPermission) {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            return@Button
                        }

                        lastSavedDistanceKm = null
                        lastSavedDurationSeconds = null
                        lastSavedPace = null

                        elapsedSeconds = 0L
                        distanceMeters = 0.0
                        locationPoints.clear()
                        runStartTimeMillis = System.currentTimeMillis()
                        isRunning = true
                        isPaused = false
                        startLocationUpdates()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Start Run",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isPaused = !isPaused
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text(if (isPaused) "Resume" else "Pause")
                    }

                    Button(
                        onClick = {
                            stopLocationUpdates()
                            resetRunState()
                            lastSavedDistanceKm = null
                            lastSavedDurationSeconds = null
                            lastSavedPace = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = red)
                    ) {
                        Text("Cancel")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val runEndTimeMillis = System.currentTimeMillis()
                        val distanceKm = distanceMeters / 1000.0
                        val pace = calculatePaceValue(elapsedSeconds, distanceMeters)

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            stopLocationUpdates()
                            resetRunState()
                            return@Button
                        }

                        val currentDate = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(Date())

                        val localRun = LocalRunSession(
                            runId = UUID.randomUUID().toString(),
                            userId = currentUser.uid,
                            date = currentDate,
                            startTime = runStartTimeMillis,
                            endTime = runEndTimeMillis,
                            durationSeconds = elapsedSeconds.toInt(),
                            distanceKm = distanceKm,
                            paceMinPerKm = pace,
                            mode = "normal",
                            isSynced = false
                        )

                        stopLocationUpdates()

                        lastSavedDistanceKm = distanceKm
                        lastSavedDurationSeconds = elapsedSeconds.toInt()
                        lastSavedPace = pace

                        resetRunState()

                        coroutineScope.launch {
                            localRunRepository.saveRunLocally(localRun)

                            try {
                                runSyncRepository.syncUnsyncedRuns()
                            } catch (e: Exception) {
                                // Silent fail. The run stays saved locally.
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Stop and Save",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showSavedOverlay) {
            SavedRunOverlay(
                primary = primary,
                textSoft = textSoft,
                paleGreen = paleGreen,
                distanceKm = lastSavedDistanceKm!!,
                durationSeconds = lastSavedDurationSeconds!!,
                pace = lastSavedPace!!,
                formatDuration = { formatDuration(it) },
                onClose = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun BoxScope.SavedRunOverlay(
    primary: Color,
    textSoft: Color,
    paleGreen: Color,
    distanceKm: Double,
    durationSeconds: Int,
    pace: Double,
    formatDuration: (Int) -> String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2A5A).copy(alpha = 0.18f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = primary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(paleGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E8E4E)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Run saved",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Okay, that counts.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSoft
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            RunResultRow(
                                label = "Distance",
                                value = String.format(Locale.getDefault(), "%.2f km", distanceKm)
                            )

                            RunResultRow(
                                label = "Time",
                                value = formatDuration(durationSeconds)
                            )

                            RunResultRow(
                                label = "Pace",
                                value = String.format(Locale.getDefault(), "%.2f min/km", pace)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FunMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(104.dp)
            .border(
                width = 2.dp,
                color = color.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4B3F8F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2F256F)
            )
        }
    }
}

@Composable
fun RunResultRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF4B67A1)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4B67A1)
        )
    }
}