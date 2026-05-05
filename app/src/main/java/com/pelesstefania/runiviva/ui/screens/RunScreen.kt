package com.pelesstefania.runiviva.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.LocalRunRepository
import com.pelesstefania.runiviva.model.LocalRunSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun RunScreen() {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFD9F0FF)
    val cardColor = Color(0xFFEAF6FF)

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val localRunRepository = remember { LocalRunRepository(context) }
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
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (!granted) {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var distanceMeters by remember { mutableStateOf(0.0) }
    var runStartTimeMillis by remember { mutableLongStateOf(0L) }

    val locationPoints = remember { mutableStateListOf<Location>() }

    fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatDistance(meters: Double): String {
        return String.format(Locale.getDefault(), "%.2f km", meters / 1000.0)
    }

    fun calculatePace(seconds: Long, meters: Double): String {
        if (meters <= 0.0) return "0.00 min/km"
        val km = meters / 1000.0
        val minutes = seconds / 60.0
        val pace = minutes / km
        return String.format(Locale.getDefault(), "%.2f min/km", pace)
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Run",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(elapsedSeconds),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatDistance(distanceMeters),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = calculatePace(elapsedSeconds, distanceMeters),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        isRunning && !isPaused -> "Running"
                        isRunning && isPaused -> "Paused"
                        else -> "Ready"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isRunning) {
            Button(
                onClick = {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        return@Button
                    }

                    elapsedSeconds = 0L
                    distanceMeters = 0.0
                    locationPoints.clear()
                    runStartTimeMillis = System.currentTimeMillis()
                    isRunning = true
                    isPaused = false
                    startLocationUpdates()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Start Run")
            }
        } else {
            Button(
                onClick = {
                    isPaused = !isPaused
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(if (isPaused) "Resume" else "Pause")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val runEndTimeMillis = System.currentTimeMillis()
                    val distanceKm = distanceMeters / 1000.0

                    val pace =
                        if (distanceKm > 0) {
                            (elapsedSeconds / 60.0) / distanceKm
                        } else {
                            0.0
                        }

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser == null) {
                        Toast.makeText(context, "No logged in user", Toast.LENGTH_SHORT).show()
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

                    isRunning = false
                    isPaused = false
                    stopLocationUpdates()

                    coroutineScope.launch {
                        localRunRepository.saveRunLocally(localRun)
                        Toast.makeText(
                            context,
                            "Run saved locally",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Stop Run")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}