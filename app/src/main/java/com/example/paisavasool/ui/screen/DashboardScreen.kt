package com.example.paisavasool.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paisavasool.ui.components.CalendarView
import com.example.paisavasool.ui.components.TransactionItem
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
) {
    val isIncomeTrackingEnabled by viewModel.isIncomeTrackingEnabled.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val scrollState = rememberScrollState()

    var selectedDate by remember { mutableStateOf(Date()) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var showCalendar by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 120, pageCount = { 240 })

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(value = false) }
    var editingTransactionId by remember { mutableStateOf<Int?>(null) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                editingTransactionId = null
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            AddTransactionForm(
                viewModel = viewModel,
                transactionId = editingTransactionId
            ) {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showBottomSheet = false
                    editingTransactionId = null
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransactionId = null
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (searchQuery.isEmpty()) "PaisaVasool" else "Search Results",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (searchResults.isEmpty()) {
                        item {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(searchResults) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = {
                                    editingTransactionId = transaction.id
                                    showBottomSheet = true
                                }
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isIncomeTrackingEnabled) "Total Balance" else "Total Expenses",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isIncomeTrackingEnabled) "₹$balance" else "₹$totalExpense",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            if (isIncomeTrackingEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Income",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "₹$totalIncome",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Expenses",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "₹$totalExpense",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        val pageMonthDate = remember(page) {
                            Calendar.getInstance().apply {
                                add(Calendar.MONTH, page - 120)
                            }.time
                        }

                        val pageTransactions = remember(transactions, selectedDate, pageMonthDate, showCalendar) {
                            transactions.filter { transaction ->
                                if (showCalendar) {
                                    isSameDay(Date(transaction.timestamp), selectedDate) && 
                                    isSameMonth(Date(transaction.timestamp), pageMonthDate)
                                } else {
                                    isSameMonth(Date(transaction.timestamp), pageMonthDate)
                                }
                            }.sortedByDescending { it.timestamp }
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(pageMonthDate),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { showCalendar = !showCalendar }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Toggle Calendar",
                                        tint = if (showCalendar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (showCalendar) {
                                CalendarView(
                                    selectedDate = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    transactions = transactions,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    viewMonth = pageMonthDate
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (showCalendar) 
                                    "Transactions for ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(selectedDate)}"
                                else 
                                    "All Transactions in ${SimpleDateFormat("MMM", Locale.getDefault()).format(pageMonthDate)}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (pageTransactions.isEmpty()) {
                                Text(
                                    text = "No transactions found",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    pageTransactions.forEach { transaction ->
                                        TransactionItem(
                                            transaction = transaction,
                                            onClick = { 
                                                editingTransactionId = transaction.id
                                                showBottomSheet = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
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

private fun isSameMonth(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}
