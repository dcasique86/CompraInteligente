package com.example.data

import android.content.Context
import android.content.SharedPreferences

class CurrencyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "currency_converter_prefs"
        private const val KEY_COP_RATE = "cop_exchange_rate"
        private const val KEY_VES_RATE = "ves_exchange_rate"

        const val DEFAULT_COP_RATE = 4000.0
        const val DEFAULT_VES_RATE = 36.50
    }

    fun getCopRate(): Double {
        val rateStr = prefs.getString(KEY_COP_RATE, DEFAULT_COP_RATE.toString())
        return rateStr?.toDoubleOrNull() ?: DEFAULT_COP_RATE
    }

    fun getVesRate(): Double {
        val rateStr = prefs.getString(KEY_VES_RATE, DEFAULT_VES_RATE.toString())
        return rateStr?.toDoubleOrNull() ?: DEFAULT_VES_RATE
    }

    fun saveCopRate(rate: Double) {
        prefs.edit().putString(KEY_COP_RATE, rate.toString()).apply()
    }

    fun saveVesRate(rate: Double) {
        prefs.edit().putString(KEY_VES_RATE, rate.toString()).apply()
    }

    fun saveRates(copRate: Double, vesRate: Double) {
        prefs.edit()
            .putString(KEY_COP_RATE, copRate.toString())
            .putString(KEY_VES_RATE, vesRate.toString())
            .apply()
    }

    fun resetToDefaults() {
        prefs.edit()
            .putString(KEY_COP_RATE, DEFAULT_COP_RATE.toString())
            .putString(KEY_VES_RATE, DEFAULT_VES_RATE.toString())
            .apply()
    }
}
