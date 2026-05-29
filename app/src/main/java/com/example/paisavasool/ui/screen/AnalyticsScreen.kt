package com.example.paisavasool.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.paisavasool.data.model.TransactionType
import com.example.paisavasool.ui.components.ChartSlice
import com.example.paisavasool.ui.components.DonutChart
import com.example.paisavasool.ui.model.Category
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel
import java.util.Calendar

enum class TimeFrame {
    DAILY, WEEKLY, MONTHLY, YEARLY, ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel) {
    val isIncomeTrackingEnabled by viewModel.isIncomeTrackingEnabled.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.MONTHLY) }
    
    val scrollState = rememberScrollState()

    val filteredTransactions = transactions.filter { transaction ->
        val calendar = Calendar.getInstance()
        
        when (selectedTimeFrame) {
            TimeFrame.DAILY -> {
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
                val startOfDay = calendar.timeInMillis
                transaction.timestamp >= startOfDay
            }
            TimeFrame.WEEKLY -> {
                calendar[Calendar.DAY_OF_WEEK] = calendar.firstDayOfWeek
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
                val startOfWeek = calendar.timeInMillis
                transaction.timestamp >= startOfWeek
            }
            TimeFrame.MONTHLY -> {
                calendar[Calendar.DAY_OF_MONTH] = 1
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
                val startOfMonth = calendar.timeInMillis
                transaction.timestamp >= startOfMonth
            }
            TimeFrame.YEARLY -> {
                calendar[Calendar.DAY_OF_YEAR] = 1
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.SECOND] = 0
                calendar[Calendar.MILLISECOND] = 0
                val startOfYear = calendar.timeInMillis
                transaction.timestamp >= startOfYear
            }
            TimeFrame.ALL -> true
        }
    }

    val totalIncome = filteredTransactions.asSequence().filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.asSequence().filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    val expenseTransactions = filteredTransactions.filter { it.type == TransactionType.EXPENSE }
    val categoryBreakdown = expenseTransactions.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val chartSlices = categoryBreakdown.asSequence().map { (categoryName, amount) ->
        val category = Category.getCategoryByName(categoryName)
        ChartSlice(
            value = amount.toFloat(),
            color = category.color,
            label = category.name
        )
    }.sortedByDescending { it.value }.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Analytics", style = MaterialTheme.typography.headlineMedium)

        // Timeframe Selector
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeFrame.entries.forEachIndexed { index, timeFrame ->
                SegmentedButton(
                    selected = selectedTimeFrame == timeFrame,
                    onClick = { selectedTimeFrame = timeFrame },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeFrame.entries.size)
                ) {
                    Text(
                        text = timeFrame.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Period Balance", style = MaterialTheme.typography.titleMedium)
                Text(text = "₹$balance", style = MaterialTheme.typography.headlineLarge)
            }
        }

        if (isIncomeTrackingEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Income", style = MaterialTheme.typography.labelMedium)
                        Text(text = "₹$totalIncome", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Expense", style = MaterialTheme.typography.labelMedium)
                        Text(text = "₹$totalExpense", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Expense for Period", style = MaterialTheme.typography.labelMedium)
                    Text(text = "₹$totalExpense", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        if (expenseTransactions.isNotEmpty()) {
            Text(text = "Expense Breakdown", style = MaterialTheme.typography.titleLarge)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DonutChart(
                        slices = chartSlices,
                        totalLabel = "Period Total",
                        totalValue = "₹$totalExpense",
                        modifier = Modifier.size(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chartSlices.forEach { slice ->
                            CategoryLegendItem(slice, totalExpense)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun CategoryLegendItem(slice: ChartSlice, totalExpense: Double) {
    val percentage = if (totalExpense > 0) (slice.value / totalExpense) * 100 else 0.0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(slice.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = slice.label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "₹${slice.value.toInt()} (${percentage.toInt()}%)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
