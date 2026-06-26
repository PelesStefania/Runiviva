package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.WeeklyChallengeRepository
import com.pelesstefania.runiviva.model.WeeklyChallengeEntry
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun WeeklyChallengeScreen() {
    val bgTop = Color(0xFFD9F0FF)
    val bgBottom = Color(0xFFEAF6FF)
    val primary = Color(0xFF4B67A1)
    val darkBlue = Color(0xFF2F3E75)
    val cardColor = Color.White
    val softBlue = Color(0xFFF4F8FF)

    val repository = remember { WeeklyChallengeRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser

    var entries by remember { mutableStateOf<List<WeeklyChallengeEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val weekNumber = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        try {
            entries = repository.getWeeklyChallenge(currentUser.uid)
            isOffline = false
        } catch (e: Exception) {
            entries = emptyList()
            isOffline = true
        }

        isLoading = false
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
            text = "Weekly Challenge",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Week $weekNumber • ${today.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = darkBlue
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (isLoading) {
            CircularProgressIndicator(color = primary)
            return@Column
        }

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
                        text = "Weekly challenge is unavailable right now.",
                        color = primary.copy(alpha = 0.75f)
                    )
                }
            }

            return@Column
        }


        entries.forEachIndexed { index, entry ->
            WeeklyChallengeCard(
                position = index + 1,
                entry = entry,
                isPodium = index < 3,
                primary = primary,
                darkBlue = darkBlue,
                cardColor = cardColor,
                softBlue = softBlue
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun WeeklyChallengeCard(
    position: Int,
    entry: WeeklyChallengeEntry,
    isPodium: Boolean,
    primary: Color,
    darkBlue: Color,
    cardColor: Color,
    softBlue: Color
) {
    val medal = when (position) {
        1 -> "\uD83E\uDD47"
        2 -> "\uD83E\uDD48"
        3 -> "\uD83E\uDD49"
        else -> "$position."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isPodium) 30.dp else 22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPodium) cardColor else softBlue
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPodium) 7.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isPodium) 20.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$medal ${entry.username}",
                    style = if (isPodium) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = darkBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = String.format(Locale.getDefault(), "%.2f km", entry.distanceKm),
                    style = if (isPodium) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = primary
                )
            }

            Text(
                text = formatChallengeDuration(entry.durationSeconds),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = primary.copy(alpha = 0.75f)
            )
        }
    }
}

fun formatChallengeDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    return when {
        hours > 0 -> "${hours}h ${minutes}min"
        minutes > 0 -> "${minutes}min"
        else -> "0min"
    }
}