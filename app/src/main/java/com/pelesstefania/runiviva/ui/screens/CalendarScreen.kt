package com.pelesstefania.runiviva.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.pelesstefania.runiviva.data.CalendarStatusRepository
import com.pelesstefania.runiviva.data.LocalRunRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen() {

    val bgTop = Color(0xFFD9F0FF)
    val bgBottom = Color(0xFFEAF6FF)

    val primary = Color(0xFF4B67A1)
    val softBlue = Color(0xFFF4F8FF)
    val borderBlue = Color(0xFFD8E8FA)

    val runGreen = Color(0xFF5DBB73)
    val sickYellow = Color(0xFFFFD66B)

    val cardWhite = Color.White

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val localRunRepository = remember {
        LocalRunRepository(context)
    }

    val calendarStatusRepository = remember {
        CalendarStatusRepository(context)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    val today = LocalDate.now()

    var displayedMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    var runDates by remember {
        mutableStateOf(setOf<String>())
    }

    var sickDates by remember {
        mutableStateOf(setOf<String>())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        coroutineScope.launch {

            calendarStatusRepository
                .syncUnsyncedStatuses(currentUser.uid)

            calendarStatusRepository
                .restoreStatusesFromFirebase(currentUser.uid)

            runDates = localRunRepository
                .getRunDates(currentUser.uid)
                .toSet()

            sickDates = calendarStatusRepository
                .getStatusesForUser(currentUser.uid)
                .filter { it.status == "sick" }
                .map { it.date }
                .toSet()

            isLoading = false
        }
    }

    val monthName = displayedMonth.month.getDisplayName(
        TextStyle.FULL,
        Locale.getDefault()
    )

    val daysInMonth = displayedMonth.lengthOfMonth()

    val firstDayOfMonth = displayedMonth.atDay(1)

    val startOffset = firstDayOfMonth.dayOfWeek.value - 1

    val totalCells = startOffset + daysInMonth

    val rows = (totalCells + 6) / 7

    val runDaysThisMonth = runDates.count {
        it.startsWith(displayedMonth.toString())
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
            text = "Calendar",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = primary
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = cardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            displayedMonth = displayedMonth.minusMonths(1)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(softBlue)
                    ) {

                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = primary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "$monthName ${displayedMonth.year}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = primary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "$runDaysThisMonth run days",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = primary.copy(alpha = 0.75f)
                        )
                    }

                    IconButton(
                        onClick = {
                            displayedMonth = displayedMonth.plusMonths(1)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(softBlue)
                    ) {

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator(color = primary)
                    }

                } else {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = softBlue,
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(12.dp)
                    ) {

                        for (row in 0 until rows) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                for (column in 0 until 7) {

                                    val cellIndex = row * 7 + column

                                    val dayNumber =
                                        cellIndex - startOffset + 1

                                    if (dayNumber in 1..daysInMonth) {

                                        val date =
                                            displayedMonth.atDay(dayNumber)

                                        val dateString =
                                            date.toString()

                                        val didRun =
                                            runDates.contains(dateString)

                                        val isSick =
                                            sickDates.contains(dateString)

                                        val isToday =
                                            date == today

                                        CalendarDayCell(
                                            dayNumber = dayNumber,
                                            didRun = didRun,
                                            isToday = isToday,
                                            isSick = isSick,
                                            primary = primary,
                                            runGreen = runGreen,
                                            sickYellow = sickYellow,
                                            borderBlue = borderBlue,
                                            modifier = Modifier.weight(1f),
                                            onClick = {

                                                coroutineScope.launch {

                                                    if (isSick) {

                                                        calendarStatusRepository
                                                            .removeSickDay(
                                                                currentUser!!.uid,
                                                                dateString
                                                            )

                                                        sickDates =
                                                            sickDates - dateString

                                                    } else {

                                                        calendarStatusRepository
                                                            .markSickDay(
                                                                currentUser!!.uid,
                                                                dateString
                                                            )

                                                        sickDates =
                                                            sickDates + dateString
                                                    }
                                                }
                                            }
                                        )

                                    } else {

                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                            }

                            if (row != rows - 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(runGreen)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    Text(
                        text = "Run day",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )

                    Spacer(modifier = Modifier.padding(10.dp))

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(sickYellow)
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    Text(
                        text = "Sick day",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    didRun: Boolean,
    isToday: Boolean,
    isSick: Boolean,
    primary: Color,
    runGreen: Color,
    sickYellow: Color,
    borderBlue: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = if (
                    isToday &&
                    !didRun &&
                    !isSick
                ) 2.dp else 1.dp,
                color = if (
                    isToday &&
                    !didRun &&
                    !isSick
                ) {
                    primary
                } else {
                    borderBlue
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(
                    if (didRun || isSick) 36.dp else 34.dp
                )
                .clip(CircleShape)
                .background(
                    when {
                        didRun -> runGreen
                        isSick -> sickYellow
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (
                    didRun ||
                    isToday ||
                    isSick
                ) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.Medium
                },
                color = if (
                    didRun ||
                    isSick
                ) {
                    Color.White
                } else {
                    primary
                }
            )
        }
    }
}