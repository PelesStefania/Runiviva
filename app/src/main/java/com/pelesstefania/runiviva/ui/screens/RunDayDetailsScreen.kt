package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.LocalRunRepository
import com.pelesstefania.runiviva.model.LocalRunSession
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunDayDetailsScreen(
    navController: NavController,
    date: String
) {
    val bgTop = Color(0xFFD9F0FF)
    val bgBottom = Color(0xFFEAF6FF)
    val primary = Color(0xFF4B67A1)
    val darkBlue = Color(0xFF2F3E75)
    val cardColor = Color.White
    val softBlue = Color(0xFFF4F8FF)

    val context = LocalContext.current
    val localRunRepository = remember { LocalRunRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var runs by remember { mutableStateOf<List<LocalRunSession>>(emptyList()) }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return if (minutes > 0) {
            if (remainingSeconds > 0) "$minutes min $remainingSeconds sec" else "$minutes min"
        } else {
            "$seconds sec"
        }
    }

    fun formatTime(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }

    LaunchedEffect(date) {
        if (currentUser == null) return@LaunchedEffect

        coroutineScope.launch {
            runs = localRunRepository.getRunsForUserByDate(
                userId = currentUser.uid,
                date = date
            )
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
            modifier = Modifier.fillMaxWidth(),
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

            Column {
                Text(
                    text = "Run day",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = primary
                )

                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyLarge,
                    color = darkBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (runs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Text(
                    text = "No runs found for this day.",
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                            text = "${formatTime(run.startTime)} - ${formatTime(run.endTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = darkBlue
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = softBlue)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                RunDetailRow(
                                    label = "Distance",
                                    value = String.format(
                                        Locale.getDefault(),
                                        "%.2f km",
                                        run.distanceKm
                                    )
                                )

                                RunDetailRow(
                                    label = "Duration",
                                    value = formatDuration(run.durationSeconds)
                                )

                                RunDetailRow(
                                    label = "Average pace",
                                    value = String.format(
                                        Locale.getDefault(),
                                        "%.2f min/km",
                                        run.paceMinPerKm
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Graph coming after we save run points.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = primary.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RunDetailRow(
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