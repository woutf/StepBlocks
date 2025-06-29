package com.stepblocks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarView(
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateClick = { date ->
                selectedDate = date
                onDateSelected(date)
            }
        )
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPreviousMonth) {
            Text("<")
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            fontWeight = FontWeight.Bold
        )
        Button(onClick = onNextMonth) {
            Text(">")
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
    val firstDayOffset = (firstDayOfMonth.value - DayOfWeek.MONDAY.value + 7) % 7

    Column {
        // Days of the week header
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = DayOfWeek.values()
            for (day in days) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days
        val weeks = (daysInMonth + firstDayOffset + 6) / 7
        for (week in 0 until weeks) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0 until 7) {
                    val dayOfMonth = week * 7 + dayOfWeek - firstDayOffset + 1
                    val date = if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                        currentMonth.atDay(dayOfMonth)
                    } else {
                        null
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                when {
                                    date == null -> Color.Transparent
                                    date == selectedDate -> Color.Blue.copy(alpha = 0.5f)
                                    else -> Color.LightGray.copy(alpha = 0.2f)
                                }
                            )
                            .clickable(enabled = date != null) {
                                date?.let { onDateClick(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            Text(
                                text = dayOfMonth.toString()
                            )
                        }
                    }
                }
            }
        }
    }
}
