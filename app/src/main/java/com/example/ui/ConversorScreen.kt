package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversionHistory
import com.example.ui.theme.FlagCopYellow
import com.example.ui.theme.FlagUsdBlue
import com.example.ui.theme.FlagVesRed
import com.example.viewmodel.ActiveCurrencyField
import com.example.viewmodel.ConverterUiState
import com.example.viewmodel.ConverterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversorScreen(
    viewModel: ConverterViewModel,
    onNavigateToProducts: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Conversor, 1: Historial
    var showInfoDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccessMessage) {
        uiState.saveSuccessMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.dismissSaveMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTab == 0) "Conversor Divisas" else "Historial de Conversiones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = { viewModel.onClearAllClicked() },
                            modifier = Modifier.testTag("clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar campos",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        if (historyList.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearHistoryDialog = true },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar historial",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Información",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                if (onNavigateToProducts != null) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToProducts,
                        icon = { Icon(Icons.Default.Home, contentDescription = "Productos") },
                        label = { Text("Productos") }
                    )
                }
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Conversor"
                        )
                    },
                    label = { Text("Conversor") },
                    modifier = Modifier.testTag("tab_conversor")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge {
                                        Text(historyList.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial"
                            )
                        }
                    },
                    label = { Text("Historial") },
                    modifier = Modifier.testTag("tab_historial")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == 0) {
                // TAB 0: CONVERSOR
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: Manual Exchange Rates Configuration
                    ExchangeRatesConfigCard(
                        uiState = uiState,
                        onCopRateChanged = { viewModel.onCopRateChanged(it) },
                        onVesRateChanged = { viewModel.onVesRateChanged(it) },
                        onResetClicked = { viewModel.onResetRatesClicked() }
                    )

                    // Section 2: Quick Amount Chips
                    QuickAmountChips(
                        onAmountSelected = { viewModel.onQuickAmountClicked(it) }
                    )

                    // Section 3: Reactive Conversion Currency Input Cards
                    CurrencyInputsSection(
                        uiState = uiState,
                        onUsdChanged = { viewModel.onUsdChanged(it) },
                        onCopChanged = { viewModel.onCopChanged(it) },
                        onVesChanged = { viewModel.onVesChanged(it) },
                        onCopyClicked = { text, label ->
                            copyToClipboard(context, text, label)
                        },
                        onClearField = { field ->
                            when (field) {
                                ActiveCurrencyField.USD -> viewModel.onUsdChanged("")
                                ActiveCurrencyField.COP -> viewModel.onCopChanged("")
                                ActiveCurrencyField.VES -> viewModel.onVesChanged("")
                                ActiveCurrencyField.NONE -> {}
                            }
                        }
                    )

                    // Section 4: Live Summary Card & Save Action
                    SummaryCard(
                        uiState = uiState,
                        onCopySummary = {
                            val summaryText = "Conversión: ${uiState.usdInput} USD = ${uiState.copInput} COP = ${uiState.vesInput} VES (Tasa COP: ${uiState.copRateInput}, VES: ${uiState.vesRateInput})"
                            copyToClipboard(context, summaryText, "Resumen de conversión")
                        },
                        onSaveHistory = {
                            viewModel.saveCurrentConversion()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // TAB 1: HISTORIAL
                HistorySection(
                    historyList = historyList,
                    onRestore = { historyItem ->
                        viewModel.restoreHistoryItem(historyItem)
                        selectedTab = 0 // Switch back to converter tab
                    },
                    onDelete = { id -> viewModel.deleteHistoryItem(id) },
                    onCopy = { text -> copyToClipboard(context, text, "Historial") },
                    onClearAll = { showClearHistoryDialog = true }
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text("Acerca del Conversor de Divisas", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Configuración de Tasas: Ingresa el valor en COP y VES correspondiente a 1 USD.")
                    Text("• Persistencia Local: Las tasas se guardan localmente en SharedPreferences y el historial en la base de datos Room de tu dispositivo.")
                    Text("• Conversión Bidireccional: Al modificar USD, COP o VES, los otros campos se recalculan inmediatamente en tiempo real usando USD como moneda puente.")
                    Text("• Historial de Conversiones: Guarda tus conversiones recientes con su fecha y hora, e impórtalas nuevamente al conversor con un solo toque.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("¿Borrar todo el historial?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción eliminará de forma permanente todas las conversiones guardadas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar Todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ExchangeRatesConfigCard(
    uiState: ConverterUiState,
    onCopRateChanged: (String) -> Unit,
    onVesRateChanged: (String) -> Unit,
    onResetClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rates_config_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tasas de Cambio Manuales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Guardado Local",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Ingresa el precio de 1 Dólar (USD) en Pesos y Bolívares:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rate 1 USD = COP
                OutlinedTextField(
                    value = uiState.copRateInput,
                    onValueChange = onCopRateChanged,
                    label = { Text("1 USD = COP") },
                    placeholder = { Text("4000.00") },
                    leadingIcon = { FlagBadge(emoji = "🇨🇴", color = FlagCopYellow) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cop_rate_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Rate 1 USD = VES
                OutlinedTextField(
                    value = uiState.vesRateInput,
                    onValueChange = onVesRateChanged,
                    label = { Text("1 USD = VES") },
                    placeholder = { Text("36.50") },
                    leadingIcon = { FlagBadge(emoji = "🇻🇪", color = FlagVesRed) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ves_rate_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            AnimatedVisibility(visible = uiState.rateError != null) {
                uiState.rateError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onResetClicked,
                    modifier = Modifier.testTag("reset_rates_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restablecer Tasas por Defecto", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickAmountChips(
    onAmountSelected: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Montos rápidos en USD:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10.0, 50.0, 100.0, 500.0, 1000.0).forEach { amount ->
                val label = if (amount >= 1000) "$1.000" else "$${amount.toInt()}"
                AssistChip(
                    onClick = { onAmountSelected(amount) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CurrencyInputsSection(
    uiState: ConverterUiState,
    onUsdChanged: (String) -> Unit,
    onCopChanged: (String) -> Unit,
    onVesChanged: (String) -> Unit,
    onCopyClicked: (String, String) -> Unit,
    onClearField: (ActiveCurrencyField) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Conversión Bidireccional",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "En tiempo real",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Input 1: USD
        CurrencyInputCard(
            currencyCode = "USD",
            currencyName = "Dólar Estadounidense",
            currencySymbol = "$",
            flagEmoji = "🇺🇸",
            flagColor = FlagUsdBlue,
            value = uiState.usdInput,
            onValueChange = onUsdChanged,
            isActive = uiState.activeField == ActiveCurrencyField.USD,
            onCopy = { onCopyClicked("${uiState.usdInput} USD", "Monto USD") },
            onClear = { onClearField(ActiveCurrencyField.USD) },
            testTag = "usd_input"
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Moneda Puente",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Input 2: COP
        CurrencyInputCard(
            currencyCode = "COP",
            currencyName = "Peso Colombiano",
            currencySymbol = "$",
            flagEmoji = "🇨🇴",
            flagColor = FlagCopYellow,
            value = uiState.copInput,
            onValueChange = onCopChanged,
            isActive = uiState.activeField == ActiveCurrencyField.COP,
            onCopy = { onCopyClicked("${uiState.copInput} COP", "Monto COP") },
            onClear = { onClearField(ActiveCurrencyField.COP) },
            testTag = "cop_input"
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Moneda Puente",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Input 3: VES
        CurrencyInputCard(
            currencyCode = "VES",
            currencyName = "Bolívar Venezolano",
            currencySymbol = "Bs.",
            flagEmoji = "🇻🇪",
            flagColor = FlagVesRed,
            value = uiState.vesInput,
            onValueChange = onVesChanged,
            isActive = uiState.activeField == ActiveCurrencyField.VES,
            onCopy = { onCopyClicked("${uiState.vesInput} VES", "Monto VES") },
            onClear = { onClearField(ActiveCurrencyField.VES) },
            testTag = "ves_input"
        )
    }
}

@Composable
private fun CurrencyInputCard(
    currencyCode: String,
    currencyName: String,
    currencySymbol: String,
    flagEmoji: String,
    flagColor: Color,
    value: String,
    onValueChange: (String) -> Unit,
    isActive: Boolean,
    onCopy: () -> Unit,
    onClear: () -> Unit,
    testTag: String
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlagBadge(emoji = flagEmoji, color = flagColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currencyCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currencyName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar $currencyCode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar $currencyCode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                placeholder = { Text("0.00", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                prefix = {
                    Text(
                        text = "$currencySymbol ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun FlagBadge(emoji: String, color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 18.sp)
    }
}

@Composable
private fun SummaryCard(
    uiState: ConverterUiState,
    onCopySummary: () -> Unit,
    onSaveHistory: () -> Unit
) {
    val copRateDouble = uiState.copRateInput.toDoubleOrNull() ?: 0.0
    val vesRateDouble = uiState.vesRateInput.toDoubleOrNull() ?: 0.0

    val crossRateStr = if (copRateDouble > 0 && vesRateDouble > 0) {
        val vesPer1000Cop = (vesRateDouble / copRateDouble) * 1000.0
        String.format(Locale.US, "1.000 COP ≈ %.2f VES", vesPer1000Cop)
    } else "Tasa no disponible"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen de Equivalencia",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(
                    onClick = onCopySummary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar resumen",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "1 USD = ${uiState.copRateInput} COP = ${uiState.vesRateInput} VES",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Relación directa: $crossRateStr",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onSaveHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_conversion_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar en Historial", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HistorySection(
    historyList: List<ConversionHistory>,
    onRestore: (ConversionHistory) -> Unit,
    onDelete: (Long) -> Unit,
    onCopy: (String) -> Unit,
    onClearAll: () -> Unit
) {
    if (historyList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "Sin Historial Aún",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Las conversiones que guardes aparecerán aquí con su fecha, valores y tasas exactas para consultas posteriores.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("history_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Conversiones Guardadas (${historyList.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onClearAll) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar todo", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            items(
                items = historyList,
                key = { it.id }
            ) { historyItem ->
                HistoryItemCard(
                    item = historyItem,
                    onRestore = { onRestore(historyItem) },
                    onDelete = { onDelete(historyItem.id) },
                    onCopy = {
                        val text = "Conversión del ${formatTimestamp(historyItem.timestamp)}: $${formatVal(historyItem.usdAmount)} USD = $${formatVal(historyItem.copAmount)} COP = Bs.${formatVal(historyItem.vesAmount)} VES"
                        onCopy(text)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: ConversionHistory,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Timestamp & Source Currency Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(item.timestamp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (item.sourceCurrency) {
                        "COP" -> FlagCopYellow.copy(alpha = 0.2f)
                        "VES" -> FlagVesRed.copy(alpha = 0.2f)
                        else -> FlagUsdBlue.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "Origen: ${item.sourceCurrency}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Values row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("🇺🇸 USD", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$ ${formatVal(item.usdAmount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("🇨🇴 COP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$ ${formatVal(item.copAmount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("🇻🇪 VES", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Bs. ${formatVal(item.vesAmount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Rates used
            Text(
                text = "Tasas usadas: 1 USD = ${formatVal(item.copRate)} COP  |  1 USD = ${formatVal(item.vesRate)} VES",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = onRestore,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cargar en conversor", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copiado al portapapeles", Toast.LENGTH_SHORT).show()
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatVal(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val symbols = java.text.DecimalFormatSymbols(Locale("es", "CO")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    return if (value % 1.0 == 0.0) {
        java.text.DecimalFormat("#,##0", symbols).format(value)
    } else {
        java.text.DecimalFormat("#,##0.00", symbols).format(value)
    }
}
