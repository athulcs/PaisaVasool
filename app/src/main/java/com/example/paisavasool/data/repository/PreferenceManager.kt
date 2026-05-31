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

    private val _isLoggedIn = MutableStateFlow(isLoggedIn())
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(loggedIn: Boolean, userName: String? = null, userEmail: String? = null) {
        sharedPreferences.edit {
            putBoolean("is_logged_in", loggedIn)
            putString("user_name", userName)
            putString("user_email", userEmail)
        }
        _isLoggedIn.value = loggedIn
    }

    fun getUserName(): String? = sharedPreferences.getString("user_name", null)
    fun getUserEmail(): String? = sharedPreferences.getString("user_email", null)

    fun isIncomeTrackingEnabled(): Boolean {
        return sharedPreferences.getBoolean("income_tracking_enabled", true)
    }

    fun setIncomeTrackingEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("income_tracking_enabled", enabled) }
        _isIncomeTrackingEnabled.value = enabled
    }
}
