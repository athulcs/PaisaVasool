package com.example.paisavasool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paisavasool.data.model.Transaction
import com.example.paisavasool.data.model.TransactionType
import com.example.paisavasool.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalIncome: StateFlow<Double> = repository.totalIncome.map { it ?: 0.0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val totalExpense: StateFlow<Double> = repository.totalExpense.map { it ?: 0.0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val balance: StateFlow<Double> = repository.allTransactions.map { transactions ->
        transactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addTransaction(title: String, amount: Double, category: String, type: TransactionType) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                category = category,
                type = type
            )
            repository.insert(transaction)
        }
    }

    fun updateTransaction(id: Int, title: String, amount: Double, category: String, type: TransactionType, timestamp: Long) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = id,
                title = title,
                amount = amount,
                timestamp = timestamp,
                category = category,
                type = type
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
}
