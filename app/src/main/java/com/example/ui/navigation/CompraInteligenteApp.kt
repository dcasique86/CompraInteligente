package com.example.ui.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.image.ImageStorageManager
import com.example.data.repository.ImageRepositoryImpl
import com.example.di.AppContainer
import com.example.domain.repository.ImageRepository
import com.example.ui.ConversorScreen
import com.example.ui.products.NewProductScreen
import com.example.ui.products.ProductListScreen
import com.example.viewmodel.ConverterViewModel
import com.example.viewmodel.NewProductViewModel
import com.example.viewmodel.ProductListViewModel
import com.example.viewmodel.ProductViewModelFactory

private object Destinations {
    const val PRODUCTS = "products"
    const val CONVERTER = "converter"
    const val NEW_PRODUCT = "new_product"
}

@Composable
fun CompraInteligenteApp(converterViewModel: ConverterViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Destinations.PRODUCTS
    
    val context = LocalContext.current
    val imageStorageManager = remember { AppContainer.provideImageStorageManager(context) }
    val imageRepository = remember { AppContainer.provideImageRepository(context) }
    
    val factory = ProductViewModelFactory(
        AppContainer.productRepository,
        imageRepository,
        imageStorageManager
    )
    
    val showMainNavigation = route == Destinations.PRODUCTS

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (route == Destinations.PRODUCTS) {
                FloatingActionButton(onClick = { navController.navigate(Destinations.NEW_PRODUCT) }) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo producto")
                }
            }
        },
        bottomBar = {
            if (showMainNavigation) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Destinations.PRODUCTS,
                        onClick = { navController.navigateToTopLevel(Destinations.PRODUCTS) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Productos") }
                    )
                    NavigationBarItem(
                        selected = route == Destinations.CONVERTER,
                        onClick = { navController.navigateToTopLevel(Destinations.CONVERTER) },
                        icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) },
                        label = { Text("Divisas") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.PRODUCTS,
            modifier = Modifier
        ) {
            composable(Destinations.PRODUCTS) {
                val viewModel: ProductListViewModel = viewModel(factory = factory)
                val products by viewModel.products.collectAsStateWithLifecycle()
                ProductListScreen(products = products, contentPadding = innerPadding)
            }
            composable(Destinations.CONVERTER) {
                ConversorScreen(
                    viewModel = converterViewModel,
                    onNavigateToProducts = { navController.navigateToTopLevel(Destinations.PRODUCTS) },
                    modifier = Modifier
                )
            }
            composable(Destinations.NEW_PRODUCT) {
                val viewModel: NewProductViewModel = viewModel(factory = factory)
                NewProductScreen(
                    contentPadding = innerPadding,
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
