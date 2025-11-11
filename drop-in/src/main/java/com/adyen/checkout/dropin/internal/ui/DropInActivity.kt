/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RestrictTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.ui.internal.InternalCheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DropInActivity : ComponentActivity() {

    private val input: DropInResultContract.Input? by lazy {
        DropInResultContract.Input.from(intent)
    }

    private val viewModel: DropInViewModel by viewModels { DropInViewModel.Factory { input } }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InternalCheckoutTheme {
                val backStack = rememberNavBackStack(EmptyNavKey, PreselectedPaymentMethodNavKey)
                NavDisplay(
                    backStack = backStack,
                    sceneStrategy = remember { BottomSheetSceneStrategy() },
                    onBack = {
                        backStack.removeLastOrNull()

                        if (backStack.size == 1 && backStack.first() == EmptyNavKey) {
                            finish()
                        }
                    },
                    entryProvider = entryProvider {
                        entry<EmptyNavKey> {
                            // This empty entry makes sure a bottom sheet can be rendered on top of nothing
                        }

                        entry<PreselectedPaymentMethodNavKey>(
                            metadata = BottomSheetSceneStrategy.bottomSheet(),
                        ) {
                            PreselectedPaymentMethodScreen(backStack)
                        }

                        entry<PaymentMethodListNavKey>(
                            metadata = DropInTransitions.slideInAndOutVertically(),
                        ) {
                            PaymentMethodListScreen()
                        }

                        entry<ManageFavoritesNavKey> {}

                        entry<PaymentMethodNavKey> {}
                    },
                )
            }
        }
    }
}
