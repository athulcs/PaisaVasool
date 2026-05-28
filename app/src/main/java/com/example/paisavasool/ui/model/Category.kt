package com.example.paisavasool.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.paisavasool.ui.theme.CatEntertainment
import com.example.paisavasool.ui.theme.CatFood
import com.example.paisavasool.ui.theme.CatHealth
import com.example.paisavasool.ui.theme.CatOther
import com.example.paisavasool.ui.theme.CatSalary
import com.example.paisavasool.ui.theme.CatShopping
import com.example.paisavasool.ui.theme.CatTransport
import com.example.paisavasool.ui.theme.CatUtilities

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val budget: Double = 0.0
) {
    companion object {
        val Food = Category("food", "Food & Dining", Icons.Filled.Fastfood, CatFood, 8000.0)
        val Shopping = Category("shopping", "Shopping", Icons.Filled.ShoppingBag, CatShopping, 5000.0)
        val Utilities = Category("utilities", "Bills & Utilities", Icons.Filled.Receipt, CatUtilities, 4000.0)
        val Transport = Category("transport", "Transport", Icons.Filled.DirectionsCar, CatTransport, 3000.0)
        val Entertainment = Category("entertainment", "Entertainment", Icons.Filled.Movie, CatEntertainment, 4000.0)
        val Health = Category("health", "Medical & Health", Icons.Filled.LocalHospital, CatHealth, 2000.0)
        val Salary = Category("salary", "Salary & Income", Icons.Filled.MonetizationOn, CatSalary, 0.0)
        val Gifts = Category("gifts", "Gifts & Gratuity", Icons.Filled.CardGiftcard, CatEntertainment, 0.0)
        val Other = Category("other", "Other Expenses", Icons.Filled.MoreHoriz, CatOther, 2000.0)

        val allCategories = listOf(Food, Shopping, Utilities, Transport, Entertainment, Health, Salary, Gifts, Other)

        fun getCategoryByName(name: String): Category {
            return allCategories.find { it.name.equals(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true) } ?: Other
        }
    }
}
