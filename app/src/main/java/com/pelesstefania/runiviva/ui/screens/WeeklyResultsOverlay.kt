package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pelesstefania.runiviva.model.WeeklyResults
import java.util.Locale

@Composable
fun BoxScope.WeeklyResultsOverlay(
    results: WeeklyResults,
    onClose: () -> Unit
) {

    val primary = Color(0xFF4B67A1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.20f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
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
                        contentDescription = null,
                        tint = primary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Color(0xFFFFF4CC),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\uD83C\uDFC6",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Weekly Results",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    results.top3.forEachIndexed { index, entry ->

                        val medal = when (index) {
                            0 -> "\uD83E\uDD47"
                            1 -> "\uD83E\uDD48"
                            else -> "\uD83E\uDD49"
                        }

                        Text(
                            text = medal,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = entry.username,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%.2f km",
                                entry.distanceKm
                            ),
                            color = primary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "────────────",
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "You finished #${results.userRank}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.2f km",
                            results.userDistanceKm
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "See you next week.",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}