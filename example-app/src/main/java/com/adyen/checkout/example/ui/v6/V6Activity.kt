/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.AdyenPaymentFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class V6Activity : AppCompatActivity() {

    private val viewModel: V6ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold { contentPadding ->
                AdyenPaymentFlow(
                    txVariant = "mbway",
                    adyenCheckout = viewModel.createAdyenCheckout(),
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}
