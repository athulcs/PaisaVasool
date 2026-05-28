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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.paisavasool.data.model.TransactionType
import com.example.paisavasool.ui.components.ChartSlice
import com.example.paisavasool.ui.components.DonutChart
import com.example.paisavasool.ui.model.Category
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel

@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val scrollState = rememberScrollState()

    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val categoryBreakdown = expenseTransactions.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val chartSlices = categoryBreakdown.map { (categoryName, amount) ->
        val category = Category.getCategoryByName(categoryName)
        ChartSlice(
            value = amount.toFloat(),
            color = category.color,
            label = category.name
        )
    }.sortedByDescending { it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Analytics", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Current Balance", style = MaterialTheme.typography.titleMedium)
                Text(text = "₹$balance", style = MaterialTheme.typography.headlineLarge)
            }
        }

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

        if (expenseTransactions.isNotEmpty()) {
            Text(text = "Expense Breakdown", style = MaterialTheme.typography.titleLarge)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DonutChart(
                        slices = chartSlices,
                        totalLabel = "Total Expense",
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
        horizontalArrangement = Arrangement.SpaceBetween
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
