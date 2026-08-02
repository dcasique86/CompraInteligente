package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.CompraInteligenteApp
import com.example.ui.theme.CurrencyConverterTheme
import com.example.viewmodel.ConverterViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: ConverterViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CurrencyConverterTheme {
        CompraInteligenteApp(converterViewModel = viewModel)
      }
    }
  }
}
