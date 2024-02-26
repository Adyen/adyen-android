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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import com.adyen.checkout.demo.data.model.CartItem
import com.adyen.checkout.demo.data.model.StoreItem
import com.adyen.checkout.demo.service.MyStoreDemoDropInService
import com.adyen.checkout.demo.ui.MyStoreDemoUiState
import com.adyen.checkout.demo.ui.MyStoreDemoViewModel
import com.adyen.checkout.demo.ui.PaymentResultState
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.SessionDropInCallback
import com.adyen.checkout.dropin.compose.rememberLauncherForDropInResult
import com.adyen.checkout.dropin.compose.startPayment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(myStoreDemoViewModel: MyStoreDemoViewModel) {
    val state by myStoreDemoViewModel.myStoreState.collectAsState()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        LazyColumn(Modifier.weight(1f)) {
            this.items(state.shoppingCart.size) {
                CartItem(
                    item = state.shoppingCart[it],
                    myStoreDemoViewModel::addToCart,
                    myStoreDemoViewModel::removeFromCart,
                )
            }
        }
        if (state.shoppingCart.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Shopping cart is empty.")
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            onClick = myStoreDemoViewModel::startDropIn,
            enabled = state.shoppingCart.isNotEmpty(),
            shape = RoundedCornerShape(4.dp),
        ) {
            Text(text = "Checkout")
        }
    }
    HandleStartDropIn(state.uiState, myStoreDemoViewModel::onDropInResult, myStoreDemoViewModel::resultConsumed)
}

@Composable
fun CartItem(item: CartItem, onAddClick: (StoreItem) -> Unit, onRemoveClick: (StoreItem) -> Unit) {
    Card(Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            AsyncImage(
                model = item.storeItem.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.CenterVertically),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .align(Alignment.CenterVertically),
            ) {
                Text(text = item.storeItem.title, fontWeight = FontWeight.Bold)
                Text(text = item.storeItem.priceText)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = { onAddClick(item.storeItem) },
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Icon(Icons.Outlined.AddCircleOutline, contentDescription = null)
                }
                Text(text = item.count.toString(), modifier = Modifier.align(Alignment.CenterHorizontally))
                IconButton(
                    onClick = { onRemoveClick(item.storeItem) },
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun HandleStartDropIn(uiState: MyStoreDemoUiState, callback: SessionDropInCallback, resultConsumed: () -> Unit) {
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
                serviceClass = MyStoreDemoDropInService::class.java,
            )
        }

        is MyStoreDemoUiState.Result -> {
            resultConsumed()
            when (uiState.state) {
                PaymentResultState.Cancelled -> {
                    Toast.makeText(LocalContext.current, "Cancelled", Toast.LENGTH_LONG).show()
                }

                PaymentResultState.Error -> {
                    Toast.makeText(LocalContext.current, "Error", Toast.LENGTH_LONG).show()
                }

                PaymentResultState.Success -> {
                    Toast.makeText(LocalContext.current, "Finished", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
