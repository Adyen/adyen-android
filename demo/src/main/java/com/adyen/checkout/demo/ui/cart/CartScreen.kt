/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2024.
 */

package com.adyen.checkout.demo.ui.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.adyen.checkout.demo.data.model.StoreItem
import com.adyen.checkout.demo.ui.MyStoreDemoUiState
import com.adyen.checkout.demo.ui.MyStoreDemoViewModel
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.SessionDropInCallback
import com.adyen.checkout.dropin.compose.rememberLauncherForDropInResult
import com.adyen.checkout.dropin.compose.startPayment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(modifier: Modifier, myStoreDemoViewModel: MyStoreDemoViewModel) {
    val state by myStoreDemoViewModel.myStoreState.collectAsState()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(
            title = {
                Text(text = "Shopping Cart", fontWeight = FontWeight.Black)
            },
        )
        val item = state.shoppingCart
        if (item != null) {
            CartItem(modifier = modifier, item = item, myStoreDemoViewModel::removeFromCart)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = myStoreDemoViewModel::startDropIn) {
                Text(text = "Checkout")
            }
        }
    }
    HandleStartDropIn(state.uiState, myStoreDemoViewModel::onDropInResult)
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
fun HandleStartDropIn(uiState: MyStoreDemoUiState, callback: SessionDropInCallback) {
    when (uiState) {
        MyStoreDemoUiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }

        MyStoreDemoUiState.Shopping -> {}

        MyStoreDemoUiState.Error -> {
            Toast.makeText(LocalContext.current, "Error", Toast.LENGTH_LONG).show()
        }

        is MyStoreDemoUiState.StartDropIn -> {
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
