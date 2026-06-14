package com.pelesstefania.runiviva.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.pelesstefania.runiviva.MainActivity
import com.pelesstefania.runiviva.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class RunTrackingState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val startTimeMillis: Long = 0L,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null
)

class RunTrackingService : Service() {

    companion object {
        const val ACTION_START = "RUNIVIVA_ACTION_START"
        const val ACTION_PAUSE = "RUNIVIVA_ACTION_PAUSE"
        const val ACTION_RESUME = "RUNIVIVA_ACTION_RESUME"
        const val ACTION_STOP = "RUNIVIVA_ACTION_STOP"

        private const val CHANNEL_ID = "run_tracking_channel"
        private const val NOTIFICATION_ID = 1001

        private val _trackingState = MutableStateFlow(RunTrackingState())
        val trackingState: StateFlow<RunTrackingState> = _trackingState
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationPoints = mutableListOf<Location>()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val state = _trackingState.value

            if (!state.isRunning || state.isPaused) return

            result.locations.forEach { newLocation ->

                if (
                    _trackingState.value.startLatitude == null &&
                    _trackingState.value.startLongitude == null &&
                    newLocation.accuracy <= 30f
                ) {
                    _trackingState.value = _trackingState.value.copy(
                        startLatitude = newLocation.latitude,
                        startLongitude = newLocation.longitude
                    )
                }

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
                        _trackingState.value = _trackingState.value.copy(
                            distanceMeters = _trackingState.value.distanceMeters + segmentDistance
                        )

                        updateNotification()
                    }
                }

                locationPoints.add(newLocation)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }

        _trackingState.value = RunTrackingState(
            isRunning = true,
            isPaused = false,
            elapsedSeconds = 0L,
            distanceMeters = 0.0,
            startTimeMillis = System.currentTimeMillis()
        )

        locationPoints.clear()

        startForeground(
            NOTIFICATION_ID,
            buildNotification()
        )

        startTimer()
        startLocationUpdates()
    }

    private fun pauseTracking() {
        _trackingState.value = _trackingState.value.copy(
            isPaused = true
        )

        timerJob?.cancel()
        updateNotification()
    }

    private fun resumeTracking() {
        _trackingState.value = _trackingState.value.copy(
            isPaused = false
        )

        startTimer()
        updateNotification()
    }

    private fun stopTracking() {
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        _trackingState.value = RunTrackingState()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = serviceScope.launch {
            while (
                _trackingState.value.isRunning &&
                !_trackingState.value.isPaused
            ) {
                delay(1000)

                _trackingState.value = _trackingState.value.copy(
                    elapsedSeconds = _trackingState.value.elapsedSeconds + 1
                )

                updateNotification()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).setMinUpdateDistanceMeters(2f).build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )
    }

    private fun buildNotification(): Notification {
        val state = _trackingState.value

        val openAppIntent = Intent(this, MainActivity::class.java)

        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val actionIntent = Intent(this, RunTrackingService::class.java).apply {
            action = if (state.isPaused) ACTION_RESUME else ACTION_PAUSE
        }

        val actionPendingIntent = PendingIntent.getService(
            this,
            1,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val actionText = if (state.isPaused) "Resume" else "Pause"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(
                if (state.isPaused) "Run paused" else "Runiviva is tracking"
            )
            .setContentText(
                "${formatTime(state.elapsedSeconds)} • ${formatDistance(state.distanceMeters)}"
            )
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.mipmap.ic_launcher,
                actionText,
                actionPendingIntent
            )
            .build()
    }

    private fun updateNotification() {
        val notificationManager =
            getSystemService(NotificationManager::class.java)

        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Run tracking",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }

    private fun formatDistance(meters: Double): String {
        return String.format(
            Locale.getDefault(),
            "%.2f km",
            meters / 1000.0
        )
    }
}