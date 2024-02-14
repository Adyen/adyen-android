/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/2/2024.
 */

package com.adyen.checkout.demo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.adyen.checkout.demo.model.StoreItem
import com.adyen.checkout.demo.ui.MyStoreViewModel.Companion.MOCK_STORE_ITEMS
import com.adyen.checkout.demo.ui.theme.ExampleTheme
import com.adyen.checkout.demo.ui.theme.LightColors
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.SessionDropInCallback
import com.adyen.checkout.dropin.compose.rememberLauncherForDropInResult
import com.adyen.checkout.dropin.compose.startPayment
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DemoActivity : ComponentActivity() {

    private val myStoreViewModel: MyStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, RedirectComponent.getReturnUrl(applicationContext))
        setContent {
            ExampleTheme {
                BasicApp(modifier = Modifier.fillMaxSize(), myStoreViewModel)
            }
        }
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}

@Composable
fun BasicApp(modifier: Modifier, myStoreViewModel: MyStoreViewModel) {
    val navController = rememberNavController()
    Scaffold(
        modifier,
        bottomBar = { BottomNavigationBar(navController = navController) },
    ) {
        NavigationHost(modifier = modifier.padding(it), navController = navController, myStoreViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    modifier: Modifier = Modifier,
    items: List<StoreItem>,
    myStoreViewModel: MyStoreViewModel
) {
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
                StoreItem(modifier = modifier, item = items[it], myStoreViewModel::addToCart)
            }
        }
    }
}

@Composable
fun StoreItem(modifier: Modifier, item: StoreItem, onClick: (StoreItem) -> Unit) {
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
            Text(text = item.priceText)
            Button(
                onClick = {
                    onClick(item)
                },
            ) {
                Text(text = "Add to Cart", fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(modifier: Modifier, myStoreViewModel: MyStoreViewModel) {
    val state by myStoreViewModel.myStoreState.collectAsState()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(
            title = {
                Text(text = "Shopping Cart", fontWeight = FontWeight.Black)
            },
        )
        val item = state.shoppingCart
        if (item != null) {
            CartItem(modifier = modifier, item = item, myStoreViewModel::removeFromCart)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = myStoreViewModel::startDropIn) {
                Text(text = "Checkout")
            }
        }
    }
    HandleStartDropIn(state.uiState, myStoreViewModel::onDropInResult)
}

@Composable
fun HandleStartDropIn(uiState: MyStoreUiState, callback: SessionDropInCallback) {
    when (uiState) {
        MyStoreUiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }

        MyStoreUiState.Shopping -> {}

        MyStoreUiState.Error -> {
            Toast.makeText(LocalContext.current, "Error", Toast.LENGTH_LONG).show()
        }

        is MyStoreUiState.StartDropIn -> {
            val launcher = rememberLauncherForDropInResult(
                callback = callback,
            )
            DropIn.startPayment(
                dropInLauncher = launcher,
                checkoutSession = uiState.session,
                checkoutConfiguration = uiState.checkoutConfiguration,
            )
        }
    }
}

@Composable
fun CartItem(modifier: Modifier, item: StoreItem, onDeleteClick: () -> Unit) {
    Card(Modifier.padding(8.dp)) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = modifier
                    .size(72.dp)
                    .align(Alignment.CenterVertically),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
            ) {
                Text(text = item.title, fontWeight = FontWeight.Bold)
                Text(text = item.priceText)
            }
            IconButton(onClick = { onDeleteClick() }, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
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
fun NavigationHost(modifier: Modifier, navController: NavHostController, myStoreViewModel: MyStoreViewModel) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.Store.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(BottomNavItem.Store.route) {
            StoreScreen(
                items = MOCK_STORE_ITEMS,
                myStoreViewModel = myStoreViewModel,
            )
        }
        composable(BottomNavItem.Cart.route) { CartScreen(modifier = Modifier, myStoreViewModel) }
    }
}

enum class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    Store("store", Icons.Default.Home, "Store"),
    Cart("cart", Icons.Default.ShoppingCart, "Cart"),
}
