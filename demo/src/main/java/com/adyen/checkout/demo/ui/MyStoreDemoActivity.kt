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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.adyen.checkout.demo.ui.compose.MyStoreDemoApp
import com.adyen.checkout.demo.ui.compose.theme.ExampleTheme
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyStoreDemoActivity : ComponentActivity() {

    private val myStoreDemoViewModel: MyStoreDemoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, RedirectComponent.getReturnUrl(applicationContext))
        setContent {
            ExampleTheme {
                MyStoreDemoApp(modifier = Modifier.fillMaxSize(), myStoreDemoViewModel)
            }
        }
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
