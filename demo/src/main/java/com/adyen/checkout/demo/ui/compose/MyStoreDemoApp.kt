/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2024.
 */

package com.adyen.checkout.demo.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.adyen.checkout.demo.navigation.NavigationHost
import com.adyen.checkout.demo.ui.MyStoreDemoViewModel

@Composable
fun MyStoreDemoApp(modifier: Modifier, myStoreDemoViewModel: MyStoreDemoViewModel) {
    val navController = rememberNavController()
    Scaffold(
        modifier,
        bottomBar = { BottomNavigationBar(navController = navController) },
    ) {
        NavigationHost(modifier = modifier.padding(it), navController = navController, myStoreDemoViewModel)
    }
}
