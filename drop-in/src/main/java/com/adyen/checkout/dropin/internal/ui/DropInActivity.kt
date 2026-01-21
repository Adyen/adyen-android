/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import com.adyen.checkout.dropin.DropInBinder
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DropInActivity : ComponentActivity() {

    private lateinit var input: DropInResultContract.Input

    private val viewModel: DropInViewModel by viewModels { DropInViewModel.Factory { input } }

    private var serviceBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            adyenLog(AdyenLogLevel.DEBUG) { "onServiceConnected" }
            val dropInBinder = binder as? DropInBinder ?: return
            viewModel.onServiceConnected(dropInBinder)

            // TODO - Implement queues and improve the queue handling if possible.
        }

        override fun onServiceDisconnected(className: ComponentName) {
            adyenLog(AdyenLogLevel.DEBUG) { "onServiceDisconnected" }
            serviceBound = false
            viewModel.onServiceDisconnected()
        }
    }

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

        startDropInService()
        bindDropInService()

        viewModel.navigator.finishFlow
            .flowWithLifecycle(lifecycle)
            .onEach { shouldFinish ->
                if (shouldFinish) {
                    stopDropInService()
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

                            entry<PaymentMethodNavKey> {}
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        unbindDropInService()
        super.onDestroy()
    }

    private fun startDropInService() {
        val intent = Intent(this, input.serviceClass)
        startService(intent)
        adyenLog(AdyenLogLevel.DEBUG) { "Started ${input.serviceClass.simpleName}" }
    }

    private fun bindDropInService() {
        val intent = Intent(this, input.serviceClass)
        val bound = bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        if (bound) {
            serviceBound = true
            adyenLog(AdyenLogLevel.DEBUG) { "Bound to ${input.serviceClass.simpleName}" }
        } else {
            adyenLog(AdyenLogLevel.ERROR) {
                "Error binding to ${input.serviceClass.simpleName}. " +
                    "The system couldn't find the service or your client doesn't have permission to bind to it"
            }
        }
    }

    private fun stopDropInService() {
        val intent = Intent(this, input.serviceClass)
        stopService(intent)
        adyenLog(AdyenLogLevel.DEBUG) { "Stopped ${input.serviceClass.simpleName}" }
    }

    private fun unbindDropInService() {
        if (serviceBound) {
            viewModel.onServiceDisconnected()
            unbindService(serviceConnection)
            serviceBound = false
            adyenLog(AdyenLogLevel.DEBUG) { "Unbound from ${input.serviceClass.simpleName}" }
        }
    }
}
