/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.ui.internal.InternalCheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DropInActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternalCheckoutTheme {
                val backStack = rememberNavBackStack(PreselectedPaymentMethodNavKey)
                NavDisplay(
                    backStack = backStack,
                    sceneStrategy = remember { BottomSheetSceneStrategy() },
                    entryProvider = entryProvider {
                        entry<PreselectedPaymentMethodNavKey>(
                            metadata = BottomSheetSceneStrategy.bottomSheet(),
                        ) {}

                        entry<PaymentMethodListNavKey> {}

                        entry<ManageFavoritesNavKey> {}

                        entry<PaymentMethodNavKey> {}
                    },
                )
            }
        }
    }
}
