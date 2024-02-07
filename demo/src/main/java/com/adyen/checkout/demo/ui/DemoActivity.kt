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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adyen.checkout.demo.ui.theme.BasicComposeTheme
import com.adyen.checkout.example.ui.demo.model.MOCK_STORE_ITEMS
import com.adyen.checkout.example.ui.demo.model.StoreItem
import com.adyen.checkout.example.ui.demo.model.formatAmount
import java.util.Locale

class DemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicComposeTheme(
                darkTheme = isSystemInDarkTheme(),
            ) {
                BasicApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun BasicApp(modifier: Modifier) {
    Surface(modifier) {
        StoreScreen(
            items = MOCK_STORE_ITEMS,
        )
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
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text(text = "Add to Cart", fontSize = 12.sp)
            }
        }
    }
}
