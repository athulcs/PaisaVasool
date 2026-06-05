package com.example.paisavasool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paisavasool.data.model.SplitType
import com.example.paisavasool.data.model.Transaction
import com.example.paisavasool.data.model.TransactionType
import com.example.paisavasool.data.repository.ExpenseRepository
import com.example.paisavasool.data.repository.PreferenceManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val isIncomeTrackingEnabled: StateFlow<Boolean> = preferenceManager.isIncomeTrackingEnabledFlow
    val isLoggedIn: StateFlow<Boolean> = preferenceManager.isLoggedInFlow

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<Transaction>> = _searchQuery
        .debounce(300)
        .combine(repository.allTransactions) { query, transactions ->
            if (query.isBlank()) {
                emptyList()
            } else {
                transactions.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                }.sortedByDescending { it.timestamp }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setIncomeTrackingEnabled(enabled: Boolean) {
        preferenceManager.setIncomeTrackingEnabled(enabled)
    }

    fun setLoggedIn(loggedIn: Boolean, userName: String? = null, userEmail: String? = null) {
        preferenceManager.setLoggedIn(loggedIn, userName, userEmail)
    }

    fun getUserName(): String? = preferenceManager.getUserName()
    fun getUserEmail(): String? = preferenceManager.getUserEmail()

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val totalExpense: StateFlow<Double> = repository.totalExpense.map { it ?: 0.0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0,
    )

    val totalIncome: StateFlow<Double> = repository.totalIncome.map { it ?: 0.0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0,
    )

    val balance: StateFlow<Double> = repository.allTransactions.map { transactions ->
        transactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0,
    )

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        timestamp: Long = System.currentTimeMillis(),
        isSplit: Boolean = false,
        splitType: SplitType = SplitType.EQUAL,
        splitValue: Double = 0.0,
        splitCount: Int = 1,
        originalAmount: Double = amount
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount,
                timestamp = timestamp,
                category = category,
                type = type,
                isSplit = isSplit,
                splitType = splitType,
                splitValue = splitValue,
                splitCount = splitCount,
                originalAmount = originalAmount
            )
            repository.insert(transaction)
        }
    }

    fun updateTransaction(
        id: Int,
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        timestamp: Long,
        isSplit: Boolean = false,
        splitType: SplitType = SplitType.EQUAL,
        splitValue: Double = 0.0,
        splitCount: Int = 1,
        originalAmount: Double = amount
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = id,
                title = title,
                amount = amount,
                timestamp = timestamp,
                category = category,
                type = type,
                isSplit = isSplit,
                splitType = splitType,
                splitValue = splitValue,
                splitCount = splitCount,
                originalAmount = originalAmount
            )
            repository.update(transaction)
        }
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return repository.getTransactionById(id)
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun deleteAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }

    fun generateMockData() {
        viewModelScope.launch {
            val categories = listOf("Food & Dining", "Shopping", "Bills & Utilities", "Transport", "Entertainment", "Medical & Health", "Other Expenses")
            val titles = mapOf(
                "Food & Dining" to listOf("Starbucks", "Dinner at Taj", "Zomato Order", "Grocery Shopping", "McDonald's"),
                "Shopping" to listOf("Amazon Purchase", "Nike Shoes", "Levi's Jeans", "H&M Shirt"),
                "Bills & Utilities" to listOf("Electricity Bill", "Water Bill", "Internet", "Mobile Recharge"),
                "Transport" to listOf("Uber Ride", "Petrol", "Metro Card Recharge", "Parking Fee"),
                "Entertainment" to listOf("Netflix Subscription", "Movie Tickets", "Bowling", "Game Pass"),
                "Medical & Health" to listOf("Pharmacy", "Doctor Consultation", "Lab Test"),
                "Other Expenses" to listOf("Gift for Friend", "Laundry", "Donation")
            )

            val calendar = Calendar.getInstance()
            val transactions = mutableListOf<Transaction>()

            // Generate transactions for the last 365 days
            for (i in 0 until 365) {
                val dayCalendar = calendar.clone() as Calendar
                dayCalendar.add(Calendar.DAY_OF_YEAR, -i)
                
                // 1-3 transactions per day
                val numTransactions = (1..3).random()
                repeat(numTransactions) {
                    val category = categories.random()
                    val title = titles[category]?.random() ?: "Random Expense"
                    val amount = (50..2000).random().toDouble()
                    
                    transactions.add(
                        Transaction(
                            title = title,
                            amount = amount,
                            timestamp = dayCalendar.timeInMillis + (0..3600000 * 12).random(), // Spread within the day
                            category = category,
                            type = TransactionType.EXPENSE
                        )
                    )
                }

                // Add monthly salary
                if (dayCalendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    transactions.add(
                        Transaction(
                            title = "Monthly Salary",
                            amount = 50000.0,
                            timestamp = dayCalendar.timeInMillis,
                            category = "Salary & Income",
                            type = TransactionType.INCOME
                        )
                    )
                }
            }

            transactions.forEach { repository.insert(it) }
        }
    }
}
