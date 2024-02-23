/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2024.
 */

package com.adyen.checkout.demo.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.adyen.checkout.demo.ui.MyStoreDemoViewModel
import com.adyen.checkout.demo.ui.cart.CartScreen
import com.adyen.checkout.demo.ui.compose.BottomNavItem
import com.adyen.checkout.demo.ui.settings.SettingsScreen
import com.adyen.checkout.demo.ui.store.StoreScreen

@Composable
fun NavigationHost(modifier: Modifier, navController: NavHostController, myStoreDemoViewModel: MyStoreDemoViewModel) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavItem.Store.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(BottomNavItem.Store.route) { StoreScreen(myStoreDemoViewModel = myStoreDemoViewModel) }
        composable(BottomNavItem.Cart.route) { CartScreen(myStoreDemoViewModel = myStoreDemoViewModel) }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(
                myStoreDemoViewModel = myStoreDemoViewModel,
                myStoreDemoViewModel::updateCountry,
            )
        }
    }
}
