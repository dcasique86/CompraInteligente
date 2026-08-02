package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ConversionHistory
import com.example.data.CurrencyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class ActiveCurrencyField {
    USD, COP, VES, NONE
}

data class ConverterUiState(
    val copRateInput: String = "4000.00",
    val vesRateInput: String = "36.50",
    val rateError: String? = null,
    val usdInput: String = "1.00",
    val copInput: String = "4000.00",
    val vesInput: String = "36.50",
    val activeField: ActiveCurrencyField = ActiveCurrencyField.USD,
    val saveSuccessMessage: String? = null
)

class ConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = CurrencyPreferences(application)
    private val _historyList = MutableStateFlow<List<ConversionHistory>>(emptyList())
    val historyList: StateFlow<List<ConversionHistory>> = _historyList.asStateFlow()

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        loadSavedRates()
    }


    private fun loadSavedRates() {
        val copRate = prefs.getCopRate()
        val vesRate = prefs.getVesRate()

        val copRateStr = formatRate(copRate)
        val vesRateStr = formatRate(vesRate)

        _uiState.update {
            it.copy(
                copRateInput = copRateStr,
                vesRateInput = vesRateStr,
                rateError = null
            )
        }
        recalculateFromCurrentActive(1.0, ActiveCurrencyField.USD)
    }

    fun onUsdChanged(newUsd: String) {
        val cleanInput = sanitizeInput(newUsd)
        val usdVal = parseToDouble(cleanInput)

        val copRate = getValidCopRate()
        val vesRate = getValidVesRate()

        val copFormatted = if (usdVal != null && copRate != null) {
            formatCurrency(usdVal * copRate, isCop = true)
        } else ""

        val vesFormatted = if (usdVal != null && vesRate != null) {
            formatCurrency(usdVal * vesRate, isCop = false)
        } else ""

        _uiState.update {
            it.copy(
                usdInput = cleanInput,
                copInput = copFormatted,
                vesInput = vesFormatted,
                activeField = ActiveCurrencyField.USD
            )
        }
    }

    fun onCopChanged(newCop: String) {
        val cleanInput = sanitizeInput(newCop)
        val copVal = parseToDouble(cleanInput)

        val copRate = getValidCopRate()
        val vesRate = getValidVesRate()

        val usdVal = if (copVal != null && copRate != null && copRate > 0) {
            copVal / copRate
        } else null

        val usdFormatted = usdVal?.let { formatCurrency(it, isCop = false) } ?: ""
        val vesFormatted = if (usdVal != null && vesRate != null) {
            formatCurrency(usdVal * vesRate, isCop = false)
        } else ""

        _uiState.update {
            it.copy(
                copInput = cleanInput,
                usdInput = usdFormatted,
                vesInput = vesFormatted,
                activeField = ActiveCurrencyField.COP
            )
        }
    }

    fun onVesChanged(newVes: String) {
        val cleanInput = sanitizeInput(newVes)
        val vesVal = parseToDouble(cleanInput)

        val copRate = getValidCopRate()
        val vesRate = getValidVesRate()

        val usdVal = if (vesVal != null && vesRate != null && vesRate > 0) {
            vesVal / vesRate
        } else null

        val usdFormatted = usdVal?.let { formatCurrency(it, isCop = false) } ?: ""
        val copFormatted = if (usdVal != null && copRate != null) {
            formatCurrency(usdVal * copRate, isCop = true)
        } else ""

        _uiState.update {
            it.copy(
                vesInput = cleanInput,
                usdInput = usdFormatted,
                copInput = copFormatted,
                activeField = ActiveCurrencyField.VES
            )
        }
    }

    fun onCopRateChanged(newRateStr: String) {
        val cleanRate = sanitizeInput(newRateStr)
        val parsedRate = parseToDouble(cleanRate)

        var error: String? = null
        if (cleanRate.isNotEmpty()) {
            if (parsedRate == null || parsedRate <= 0) {
                error = "La tasa de COP debe ser un número mayor a 0"
            } else {
                prefs.saveCopRate(parsedRate)
            }
        }

        _uiState.update {
            it.copy(
                copRateInput = cleanRate,
                rateError = error
            )
        }

        recalculatePassiveFields()
    }

    fun onVesRateChanged(newRateStr: String) {
        val cleanRate = sanitizeInput(newRateStr)
        val parsedRate = parseToDouble(cleanRate)

        var error: String? = null
        if (cleanRate.isNotEmpty()) {
            if (parsedRate == null || parsedRate <= 0) {
                error = "La tasa de VES debe ser un número mayor a 0"
            } else {
                prefs.saveVesRate(parsedRate)
            }
        }

        _uiState.update {
            it.copy(
                vesRateInput = cleanRate,
                rateError = error
            )
        }

        recalculatePassiveFields()
    }

    fun onResetRatesClicked() {
        prefs.resetToDefaults()
        loadSavedRates()
    }

    fun onClearAllClicked() {
        _uiState.update {
            it.copy(
                usdInput = "",
                copInput = "",
                vesInput = "",
                activeField = ActiveCurrencyField.NONE
            )
        }
    }

    fun onQuickAmountClicked(usdAmount: Double) {
        onUsdChanged(formatRate(usdAmount))
    }

    fun saveCurrentConversion() {
        val currentState = _uiState.value
        val usdVal = parseToDouble(currentState.usdInput) ?: 0.0
        val copVal = parseToDouble(currentState.copInput) ?: 0.0
        val vesVal = parseToDouble(currentState.vesInput) ?: 0.0
        val copRate = parseToDouble(currentState.copRateInput) ?: 0.0
        val vesRate = parseToDouble(currentState.vesRateInput) ?: 0.0

        if (usdVal <= 0 && copVal <= 0 && vesVal <= 0) {
            _uiState.update { it.copy(saveSuccessMessage = "Introduce un monto para guardar la conversión") }
            return
        }

        val sourceCurrency = when (currentState.activeField) {
            ActiveCurrencyField.COP -> "COP"
            ActiveCurrencyField.VES -> "VES"
            ActiveCurrencyField.USD, ActiveCurrencyField.NONE -> "USD"
        }

        val sourceAmount = when (currentState.activeField) {
            ActiveCurrencyField.COP -> copVal
            ActiveCurrencyField.VES -> vesVal
            ActiveCurrencyField.USD, ActiveCurrencyField.NONE -> usdVal
        }

        val history = ConversionHistory(
            sourceCurrency = sourceCurrency,
            sourceAmount = sourceAmount,
            usdAmount = usdVal,
            copAmount = copVal,
            vesAmount = vesVal,
            copRate = copRate,
            vesRate = vesRate,
            timestamp = System.currentTimeMillis()
        )

        _historyList.update { listOf(history) + it }
        _uiState.update { it.copy(saveSuccessMessage = "Conversión guardada en el historial") }
    }

    fun deleteHistoryItem(id: Long) {
        _historyList.update { history -> history.filterNot { it.id == id } }
    }

    fun clearAllHistory() {
        _historyList.value = emptyList()
    }

    fun restoreHistoryItem(item: ConversionHistory) {
        val copRateStr = formatRate(item.copRate)
        val vesRateStr = formatRate(item.vesRate)

        prefs.saveCopRate(item.copRate)
        prefs.saveVesRate(item.vesRate)

        _uiState.update {
            it.copy(
                copRateInput = copRateStr,
                vesRateInput = vesRateStr,
                usdInput = formatCurrency(item.usdAmount, isCop = false),
                copInput = formatCurrency(item.copAmount, isCop = true),
                vesInput = formatCurrency(item.vesAmount, isCop = false),
                activeField = when (item.sourceCurrency) {
                    "COP" -> ActiveCurrencyField.COP
                    "VES" -> ActiveCurrencyField.VES
                    else -> ActiveCurrencyField.USD
                },
                saveSuccessMessage = "Conversión cargada en el conversor"
            )
        }
    }

    fun dismissSaveMessage() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    private fun recalculatePassiveFields() {
        val currentState = _uiState.value
        when (currentState.activeField) {
            ActiveCurrencyField.COP -> onCopChanged(currentState.copInput)
            ActiveCurrencyField.VES -> onVesChanged(currentState.vesInput)
            ActiveCurrencyField.USD, ActiveCurrencyField.NONE -> onUsdChanged(currentState.usdInput)
        }
    }

    private fun recalculateFromCurrentActive(defaultUsdVal: Double, field: ActiveCurrencyField) {
        val copRate = getValidCopRate()
        val vesRate = getValidVesRate()

        val usdStr = formatCurrency(defaultUsdVal, isCop = false)
        val copStr = if (copRate != null) formatCurrency(defaultUsdVal * copRate, isCop = true) else ""
        val vesStr = if (vesRate != null) formatCurrency(defaultUsdVal * vesRate, isCop = false) else ""

        _uiState.update {
            it.copy(
                usdInput = usdStr,
                copInput = copStr,
                vesInput = vesStr,
                activeField = field
            )
        }
    }

    private fun getValidCopRate(): Double? {
        val parsed = parseToDouble(_uiState.value.copRateInput)
        return if (parsed != null && parsed > 0) parsed else null
    }

    private fun getValidVesRate(): Double? {
        val parsed = parseToDouble(_uiState.value.vesRateInput)
        return if (parsed != null && parsed > 0) parsed else null
    }

    private fun sanitizeInput(input: String): String {
        return input.filter { it.isDigit() || it == '.' || it == ',' }
    }

    private fun parseToDouble(input: String): Double? {
        if (input.isBlank()) return null
        val clean = input.trim()
        if (clean == "." || clean == ",") return null

        var normalized = clean

        if (normalized.contains('.') && normalized.contains(',')) {
            val lastDot = normalized.lastIndexOf('.')
            val lastComma = normalized.lastIndexOf(',')
            normalized = if (lastComma > lastDot) {
                normalized.replace(".", "").replace(',', '.')
            } else {
                normalized.replace(",", "")
            }
        } else if (normalized.contains('.')) {
            if (normalized.count { it == '.' } > 1) {
                normalized = normalized.replace(".", "")
            } else {
                val parts = normalized.split('.')
                if (parts.size == 2 && parts[1].length == 3 && parts[0].isNotEmpty()) {
                    val noDot = normalized.replace(".", "")
                    val asDecimal = normalized.toDoubleOrNull()
                    if (asDecimal != null && asDecimal < 100.0 && noDot.toDouble() >= 1000) {
                        normalized = noDot
                    }
                }
            }
        } else if (normalized.contains(',')) {
            if (normalized.count { it == ',' } > 1) {
                normalized = normalized.replace(",", "")
            } else {
                val parts = normalized.split(',')
                if (parts.size == 2 && parts[1].length == 3 && parts[0].isNotEmpty()) {
                    val noComma = normalized.replace(",", "")
                    val asDecimal = normalized.replace(',', '.').toDoubleOrNull()
                    if (asDecimal != null && asDecimal < 100.0 && noComma.toDouble() >= 1000) {
                        normalized = noComma
                    } else {
                        normalized = normalized.replace(',', '.')
                    }
                } else {
                    normalized = normalized.replace(',', '.')
                }
            }
        }

        return try {
            val valDouble = normalized.toDouble()
            if (valDouble.isNaN() || valDouble.isInfinite()) null else valDouble
        } catch (e: Exception) {
            null
        }
    }

    private fun formatCurrency(amount: Double, isCop: Boolean): String {
        if (amount.isNaN() || amount.isInfinite() || amount < 0) return ""
        val symbols = java.text.DecimalFormatSymbols(Locale("es", "CO")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        return if (isCop) {
            if (amount % 1.0 == 0.0) {
                java.text.DecimalFormat("#,##0", symbols).format(amount)
            } else {
                java.text.DecimalFormat("#,##0.00", symbols).format(amount)
            }
        } else {
            java.text.DecimalFormat("#,##0.00", symbols).format(amount)
        }
    }

    private fun formatRate(rate: Double): String {
        if (rate.isNaN() || rate.isInfinite() || rate < 0) return ""
        val symbols = java.text.DecimalFormatSymbols(Locale("es", "CO")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        return if (rate % 1.0 == 0.0) {
            java.text.DecimalFormat("#,##0", symbols).format(rate)
        } else {
            java.text.DecimalFormat("#,##0.00", symbols).format(rate)
        }
    }
}
