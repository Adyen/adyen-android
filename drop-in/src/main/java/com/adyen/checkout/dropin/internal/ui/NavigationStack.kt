/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/2/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

@Composable
internal fun NavigationStack(
    viewModel: DropInViewModel,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    NavDisplay(
        backStack = viewModel.navigator.backStack,
        sceneStrategy = remember { BottomSheetSceneStrategy() },
        onBack = { viewModel.navigator.back() },
        entryProvider = { key ->
            when (key) {
                is EmptyNavKey -> {
                    // This empty entry makes sure a bottom sheet can be rendered on top of nothing
                    NavEntry(key) {}
                }

                is PreselectedPaymentMethodNavKey -> {
                    NavEntry(
                        key = key,
                        metadata = BottomSheetSceneStrategy.bottomSheet(),
                    ) {
                        PreselectedPaymentMethodScreen(
                            viewModel(
                                factory = PreselectedPaymentMethodViewModel.Factory(
                                    dropInParams = viewModel.dropInParams,
                                    storedPaymentMethod = key.storedPaymentMethod,
                                    navigator = viewModel.navigator,
                                ),
                            ),
                        )
                    }
                }

                is PaymentMethodListNavKey -> {
                    NavEntry(
                        key = key,
                        metadata = DropInTransitions.slideInAndOutVertically(),
                    ) {
                        PaymentMethodListScreen(
                            viewModel.navigator,
                            viewModel(
                                factory = PaymentMethodListViewModel.Factory(
                                    dropInParams = viewModel.dropInParams,
                                    paymentMethodRepository = viewModel.paymentMethodRepository,
                                ),
                            ),
                        )
                    }
                }

                is ManageFavoritesNavKey -> {
                    NavEntry(
                        key = key,
                        metadata = DropInTransitions.slideInAndOutHorizontally(),
                    ) {
                        ManageFavoritesScreen(
                            navigator = viewModel.navigator,
                            viewModel = viewModel(
                                factory = ManageFavoritesViewModel.Factory(
                                    paymentMethodRepository = viewModel.paymentMethodRepository,
                                ),
                            ),
                        )
                    }
                }

                is PaymentMethodNavKey -> {
                    val transitions = if (viewModel.navigator.isEmptyAfterCurrent()) {
                        DropInTransitions.slideInAndOutVertically()
                    } else {
                        DropInTransitions.slideInAndOutHorizontally()
                    }

                    NavEntry(
                        key = key,
                        metadata = transitions,
                    ) {
                        PaymentMethodScreen(
                            navigator = viewModel.navigator,
                            viewModel = viewModel(
                                factory = PaymentMethodViewModel.Factory(
                                    paymentFlowType = key.paymentFlowType,
                                    paymentMethodRepository = viewModel.paymentMethodRepository,
                                    checkoutContext = viewModel.checkoutContext,
                                    dropInServiceManager = viewModel.dropInServiceManager,
                                ),
                                key = key.paymentFlowType.hashCode().toString(),
                            ),
                        )
                    }
                }

                else -> error("Unknown key: $key")
            }
        },
    )
}
