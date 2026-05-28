package com.example.paisavasool.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.paisavasool.data.model.TransactionType
import com.example.paisavasool.ui.model.Category
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@Composable
fun AddTransactionScreen(
    viewModel: ExpenseViewModel,
    transactionId: Int? = null,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.Other.name) }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            val transaction = viewModel.getTransactionById(transactionId)
            if (transaction != null) {
                title = transaction.title
                amount = transaction.amount.toString()
                category = transaction.category
                type = transaction.type
                timestamp = transaction.timestamp
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (transactionId == null) "Add Transaction" else "Edit Transaction",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(text = "Transaction Type", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = type == TransactionType.EXPENSE,
                onClick = { 
                    type = TransactionType.EXPENSE 
                    if (category == Category.Salary.name) {
                        category = Category.Other.name
                    }
                }
            )
            Text("Expense")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = type == TransactionType.INCOME,
                onClick = { 
                    type = TransactionType.INCOME 
                    category = Category.Salary.name
                }
            )
            Text("Income")
        }

        if (type == TransactionType.EXPENSE) {
            Text(text = "Category", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Category.allCategories.filter { it != Category.Salary }.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(cat.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.name)
                                }
                            },
                            onClick = {
                                category = cat.name
                                expanded = false
                            },
                            trailingIcon = {
                                if (category == cat.name) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotEmpty() && amountVal > 0) {
                    if (transactionId == null) {
                        viewModel.addTransaction(title, amountVal, category, type)
                    } else {
                        viewModel.updateTransaction(transactionId, title, amountVal, category, type, timestamp)
                    }
                    onBackClick()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && amount.isNotBlank()
        ) {
            Text(if (transactionId == null) "Save Transaction" else "Update Transaction")
        }

        if (transactionId != null) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val transaction = viewModel.getTransactionById(transactionId)
                        if (transaction != null) {
                            viewModel.deleteTransaction(transaction)
                            onBackClick()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Transaction")
            }
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}
