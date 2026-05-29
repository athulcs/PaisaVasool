package com.example.paisavasool.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("paisa_vasool_prefs", Context.MODE_PRIVATE)

    private val _isIncomeTrackingEnabled = MutableStateFlow(isIncomeTrackingEnabled())
    val isIncomeTrackingEnabledFlow: StateFlow<Boolean> = _isIncomeTrackingEnabled

    fun isIncomeTrackingEnabled(): Boolean {
        return sharedPreferences.getBoolean("income_tracking_enabled", true)
    }

    fun setIncomeTrackingEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("income_tracking_enabled", enabled) }
        _isIncomeTrackingEnabled.value = enabled
    }
}
