package com.example.paisavasool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.paisavasool.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    viewMonth: Date = selectedDate,
) {
    val calendar = Calendar.getInstance().apply { 
        time = viewMonth
        set(Calendar.DAY_OF_MONTH, 1) 
    }
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed, 0 = Sunday
    
    val days = mutableListOf<Date?>()
    repeat(firstDayOfWeek) {
        days.add(null)
    }
    
    val tempCal = calendar.clone() as Calendar
    for (i in 1..daysInMonth) {
        tempCal.set(Calendar.DAY_OF_MONTH, i)
        days.add(tempCal.time)
    }
    
    while ((days.size % 7) != 0) {
        days.add(null)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isSelected = isSameDay(date, selectedDate)
                            val isToday = isSameDay(date, Date())
                            val hasTransactions = transactions.any { isSameDay(Date(it.timestamp), date) }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val dayCal = Calendar.getInstance().apply { time = date }
                                    Text(
                                        text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasTransactions) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
