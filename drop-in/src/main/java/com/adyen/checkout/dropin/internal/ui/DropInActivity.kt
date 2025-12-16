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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

        viewModel.navigator.finishFlow
            .flowWithLifecycle(lifecycle)
            .onEach { shouldFinish ->
                if (shouldFinish) {
                    finish()
                }
            }
            .launchIn(lifecycleScope)

        setContent {
            InternalCheckoutTheme {
                CheckoutCompositionLocalProvider(
                    locale = viewModel.dropInParams.shopperLocale,
                    // TODO - support custom localization for drop-in
                    localizationProvider = null,
                    environment = viewModel.dropInParams.environment,
                ) {
                    NavDisplay(
                        backStack = viewModel.navigator.backStack,
                        sceneStrategy = remember { BottomSheetSceneStrategy() },
                        onBack = { viewModel.navigator.back() },
                        entryProvider = entryProvider {
                            entry<EmptyNavKey> {
                                // This empty entry makes sure a bottom sheet can be rendered on top of nothing
                            }

                            entry<PreselectedPaymentMethodNavKey>(
                                metadata = BottomSheetSceneStrategy.bottomSheet(),
                            ) { key ->
                                PreselectedPaymentMethodScreen(
                                    viewModel.navigator,
                                    viewModel(
                                        factory = PreselectedPaymentMethodViewModel.Factory(
                                            dropInParams = viewModel.dropInParams,
                                            storedPaymentMethod = key.storedPaymentMethod,
                                        ),
                                    ),
                                )
                            }

                            entry<PaymentMethodListNavKey>(
                                metadata = DropInTransitions.slideInAndOutVertically(),
                            ) {
                                PaymentMethodListScreen(
                                    viewModel.navigator,
                                    viewModel(
                                        factory = PaymentMethodListViewModel.Factory(
                                            dropInParams = viewModel.dropInParams,
                                            paymentMethodsApiResponse = viewModel.paymentMethods,
                                        ),
                                    ),
                                )
                            }

                            entry<ManageFavoritesNavKey> {
                                ManageFavoritesScreen(
                                    navigator = viewModel.navigator,
                                )
                            }

                            entry<PaymentMethodNavKey> {}
                        },
                    )
                }
            }
        }
    }
}
