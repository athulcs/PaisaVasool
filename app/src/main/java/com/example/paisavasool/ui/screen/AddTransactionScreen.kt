package com.example.paisavasool.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paisavasool.data.model.SplitType
import com.example.paisavasool.data.model.TransactionType
import com.example.paisavasool.ui.model.Category
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: ExpenseViewModel,
    transactionId: Int? = null,
    onBackClick: () -> Unit
) {
    val isIncomeTrackingEnabled by viewModel.isIncomeTrackingEnabled.collectAsState()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf(Category.Other.name) }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var isSplit by remember { mutableStateOf(false) }
    var splitCount by remember { mutableIntStateOf(2) }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var splitValue by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        timestamp = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            val transaction = viewModel.getTransactionById(transactionId)
            if (transaction != null) {
                title = transaction.title
                amount = if (transaction.amount == 0.0) "" else transaction.amount.toString()
                categoryName = transaction.category
                type = transaction.type
                timestamp = transaction.timestamp
                isSplit = transaction.isSplit
                splitCount = transaction.splitCount
                splitType = transaction.splitType
                splitValue = if (transaction.splitType == SplitType.EQUAL) "" else transaction.splitValue.toString()
                if (transaction.isSplit) {
                    amount = transaction.originalAmount.toString()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (transactionId == null) "New Transaction" else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (transactionId != null) {
                        IconButton(onClick = {
                            scope.launch {
                                val transaction = viewModel.getTransactionById(transactionId)
                                if (transaction != null) {
                                    viewModel.deleteTransaction(transaction)
                                    onBackClick()
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Transaction Type Toggle
            if (isIncomeTrackingEnabled) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = type == TransactionType.EXPENSE,
                        onClick = {
                            type = TransactionType.EXPENSE
                            if (categoryName == Category.Salary.name) {
                                categoryName = Category.Other.name
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Expense")
                    }
                    SegmentedButton(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            categoryName = Category.Salary.name
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Income")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // If income tracking is disabled, force EXPENSE type
                type = TransactionType.EXPENSE
            }

            // Amount Field
            Text(
                text = "How much?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                TextField(
                    value = amount,
                    onValueChange = { if (it.length <= 10) amount = it },
                    placeholder = { 
                        Text(
                            "0", 
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                fontSize = 32.sp
                            )
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What was it for?") },
                placeholder = { Text("e.g. Grocery, Netflix, Dinner") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selection
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth, 
                        contentDescription = "Select Date", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(timestamp)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Split Expense Option
            if (type == TransactionType.EXPENSE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Split Expense", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Divide with others", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Switch(
                        checked = isSplit,
                        onCheckedChange = { isSplit = it },
                        thumbContent = if (isSplit) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else null
                    )
                }

                if (isSplit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Split Type Selector
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SplitType.entries.forEachIndexed { index, sType ->
                                    SegmentedButton(
                                        selected = splitType == sType,
                                        onClick = { splitType = sType },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = SplitType.entries.size)
                                    ) {
                                        Text(sType.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            when (splitType) {
                                SplitType.EQUAL -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Split between", style = MaterialTheme.typography.bodySmall)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { if (splitCount > 2) splitCount-- },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text("-", style = MaterialTheme.typography.titleLarge)
                                            }
                                            Text(
                                                "$splitCount people",
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(
                                                onClick = { if (splitCount < 20) splitCount++ },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text("+", style = MaterialTheme.typography.titleLarge)
                                            }
                                        }
                                    }
                                }
                                SplitType.PERCENTAGE -> {
                                    OutlinedTextField(
                                        value = splitValue,
                                        onValueChange = { if (it.length <= 3) splitValue = it },
                                        label = { Text("Your Percentage (%)") },
                                        placeholder = { Text("e.g. 50") },
                                        modifier = Modifier.fillMaxWidth(),
                                        suffix = { Text("%") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                SplitType.CUSTOM -> {
                                    OutlinedTextField(
                                        value = splitValue,
                                        onValueChange = { splitValue = it },
                                        label = { Text("Your Share Amount (₹)") },
                                        placeholder = { Text("e.g. 500") },
                                        modifier = Modifier.fillMaxWidth(),
                                        prefix = { Text("₹") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                            
                            val totalAmount = amount.toDoubleOrNull() ?: 0.0
                            val myShare = when (splitType) {
                                SplitType.EQUAL -> totalAmount / splitCount
                                SplitType.PERCENTAGE -> (splitValue.toDoubleOrNull() ?: 0.0) / 100.0 * totalAmount
                                SplitType.CUSTOM -> splitValue.toDoubleOrNull() ?: 0.0
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Your Share", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "₹${String.format(Locale.getDefault(), "%.2f", myShare)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category Selection
            if (type == TransactionType.EXPENSE) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Using a grid for categories is much more user-friendly than a dropdown
                val expenseCategories = Category.allCategories.filter { it != Category.Salary }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Manual grid to avoid issues with verticalScroll + LazyVerticalGrid
                    val rows = expenseCategories.chunked(4)
                    rows.forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCategories.forEach { cat ->
                                CategoryChip(
                                    category = cat,
                                    isSelected = categoryName == cat.name,
                                    onClick = { categoryName = cat.name },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty slots in the last row if necessary
                            repeat(4 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                // For Income, just show Salary as selected or let them choose if there are more
                categoryName = Category.Salary.name
                Surface(
                    color = Category.Salary.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Category.Salary.color),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Category.Salary.icon, contentDescription = null, tint = Category.Salary.color, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Category.Salary.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            val isFormValid = title.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true
            
            Button(
                onClick = {
                    val originalAmountVal = amount.toDoubleOrNull() ?: 0.0
                    val finalAmount = if (isSplit) {
                        when (splitType) {
                            SplitType.EQUAL -> originalAmountVal / splitCount
                            SplitType.PERCENTAGE -> (splitValue.toDoubleOrNull() ?: 0.0) / 100.0 * originalAmountVal
                            SplitType.CUSTOM -> splitValue.toDoubleOrNull() ?: 0.0
                        }
                    } else originalAmountVal
                    
                    if (transactionId == null) {
                        viewModel.addTransaction(
                            title = title,
                            amount = finalAmount,
                            category = categoryName,
                            type = type,
                            timestamp = timestamp,
                            isSplit = isSplit,
                            splitType = splitType,
                            splitValue = splitValue.toDoubleOrNull() ?: 0.0,
                            splitCount = splitCount,
                            originalAmount = originalAmountVal
                        )
                    } else {
                        viewModel.updateTransaction(
                            id = transactionId,
                            title = title,
                            amount = finalAmount,
                            category = categoryName,
                            type = type,
                            timestamp = timestamp,
                            isSplit = isSplit,
                            splitType = splitType,
                            splitValue = splitValue.toDoubleOrNull() ?: 0.0,
                            splitCount = splitCount,
                            originalAmount = originalAmountVal
                        )
                    }
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (transactionId == null) "Add Transaction" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) category.color else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.height(68.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
