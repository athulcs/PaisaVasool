package com.example.paisavasool.utils

import com.example.paisavasool.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

object DataExportUtils {
    fun generateCsv(transactions: List<Transaction>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csvHeader = "ID,Title,Amount,Date,Category,Type,Is Split,Split Type,Split Value,Split Count,Original Amount\n"
        val csvBody = transactions.joinToString("\n") { transaction ->
            val date = dateFormat.format(Date(transaction.timestamp))
            val title = escapeCsv(transaction.title)
            val category = escapeCsv(transaction.category)
            "${transaction.id},$title,${transaction.amount},$date,$category,${transaction.type},${transaction.isSplit},${transaction.splitType},${transaction.splitValue},${transaction.splitCount},${transaction.originalAmount}"
        }
        return csvHeader + csvBody
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
