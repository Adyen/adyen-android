/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/2/2024.
 */

package com.adyen.checkout.demo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.adyen.checkout.demo.ui.theme.ExampleTheme
import com.adyen.checkout.demo.ui.theme.LightColors
import com.adyen.checkout.demo.model.MOCK_STORE_ITEMS
import com.adyen.checkout.demo.model.StoreItem
import com.adyen.checkout.demo.model.formatAmount
import java.util.Locale

class DemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExampleTheme {
                BasicApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun BasicApp(modifier: Modifier) {
    val navController = rememberNavController()
    Scaffold(
        modifier,
        bottomBar = { BottomNavigationBar(navController = navController) },
    ) {
        NavigationHost(modifier = modifier.padding(it), navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(modifier: Modifier = Modifier, items: List<StoreItem>) {
    Column {
        Surface {
            TopAppBar(
                title = {
                    Text(text = "My Store", fontWeight = FontWeight.Black)
                },
            )
        }
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
            ),
        ) {
            items(items.size) {
                StoreItem(modifier = modifier, item = items[it])
            }
        }
    }
}

@Composable
fun StoreItem(modifier: Modifier, item: StoreItem) {
    Card(
        modifier
            .padding(4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(4.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = modifier
                    .size(72.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Text(text = item.title, fontWeight = FontWeight.Bold)
            Text(text = item.price.formatAmount(Locale.US))
            Button(onClick = {}) {
                Text(text = "Add to Cart", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CartScreen(modifier: Modifier) {

}

@Composable
fun SettingsScreen(modifier: Modifier) {

}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(backgroundColor = LightColors.primary, contentColor = LightColors.onPrimary) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        BottomNavItem.entries.forEach { item ->
            BottomNavigationItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                icon = {
                    Icon(item.icon, contentDescription = null)
                },
            )
        }
    }
}

@Composable
fun NavigationHost(modifier: Modifier, navController: NavHostController) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.Store.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(BottomNavItem.Store.route) { StoreScreen(items = MOCK_STORE_ITEMS) }
        composable(BottomNavItem.Cart.route) { CartScreen(modifier = Modifier) }
        composable(BottomNavItem.Settings.route) { SettingsScreen(modifier = Modifier) }
    }
}

enum class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    Store("store", Icons.Default.Home, "Store"),
    Cart("cart", Icons.Default.ShoppingCart, "Cart"),
    Settings("settings", Icons.Default.Settings, "Settings");
}
























