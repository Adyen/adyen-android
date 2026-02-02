/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Intent
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
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DropInActivity : ComponentActivity() {

    private lateinit var input: DropInResultContract.Input

    private val viewModel: DropInViewModel by viewModels { DropInViewModel.Factory { input } }

    @Suppress("LongMethod")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val parsedInput = DropInResultContract.Input.from(intent)
        if (parsedInput == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Input is null. Closing drop-in." }
            // TODO - Return DropInResult.Failed and close drop-in
            finish()
            return
        }
        input = parsedInput

        viewModel.startDropInService(this)

        viewModel.resultFlow
            .flowWithLifecycle(lifecycle)
            .onEach { result -> sendResult(result) }
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
                                    viewModel(
                                        factory = PreselectedPaymentMethodViewModel.Factory(
                                            dropInParams = viewModel.dropInParams,
                                            storedPaymentMethod = key.storedPaymentMethod,
                                            navigator = viewModel.navigator,
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
                                            paymentMethodRepository = viewModel.paymentMethodRepository,
                                        ),
                                    ),
                                )
                            }

                            entry<ManageFavoritesNavKey>(
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

                            entry<PaymentMethodNavKey>(
                                metadata = DropInTransitions.slideInAndOutHorizontally(),
                            ) { key ->
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
                        },
                    )
                }
            }
        }
    }

    private fun sendResult(dropInResult: DropInResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "sendResult: $dropInResult" }
        val resultIntent = Intent().putExtra(
            DropInResultContract.EXTRA_RESULT,
            DropInResultContract.Result(dropInResult),
        )
        setResult(RESULT_OK, resultIntent)
        terminate()
    }

    private fun terminate() {
        adyenLog(AdyenLogLevel.DEBUG) { "terminate" }
        viewModel.stopDropInService(this)
        finish()
    }

    override fun onDestroy() {
        viewModel.unbindDropInService(this)
        if (isFinishing) {
            viewModel.stopDropInService(this)
        }
        super.onDestroy()
    }
}
