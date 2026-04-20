/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.compose.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class V6SessionsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // TODO - Replace with checkoutConfigurationProvider once it's updated COSDK-563
    private val configuration = CheckoutConfiguration(
        Environment.TEST,
        BuildConfig.CLIENT_KEY,
    )

    private lateinit var checkoutContext: CheckoutContext.Sessions

    var uiState by mutableStateOf<V6UiState>(V6UiState.Loading)

    init {
        viewModelScope.launch {
            createSession()
        }
    }

    private suspend fun createSession() {
        val sessionResponse = paymentsRepository.createSession(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getOldAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                redirectUrl = savedStateHandle.get<String>(V6SessionsActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
                showRemovePaymentMethodButton = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
            ),
        ) ?: return

        val result = Checkout.setup(
            sessionResponse = sessionResponse,
            configuration = configuration,
        )

        uiState = when (result) {
            is Checkout.Result.Error -> V6UiState.Error(UIText.String(result.error.message.orEmpty()))
            is Checkout.Result.Success -> {
                checkoutContext = result.checkoutContext
                val paymentMethods = checkoutContext.getPaymentMethods()
                V6UiState.Component(
                    paymentMethods = paymentMethods,
                    selectedPaymentMethod = paymentMethods.first(),
                    checkoutController = createCheckoutController(
                        paymentMethod = paymentMethods.first(),
                        checkoutContext = checkoutContext,
                    ),
                )
            }
        }
    }

    private fun onError(error: CheckoutError) {
        Log.d(TAG, "onError: ${error.message}")
    }

    private fun onFinished() {
        Log.d(TAG, "onFinished")
    }

    @Suppress("unused")
    fun handleIntent(intent: Intent) {
        // TODO - Check if the controller should handle the intent or if we can do this inside a component
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod) {
        val newState = (uiState as? V6UiState.Component)?.copy(
            selectedPaymentMethod = paymentMethod,
            checkoutController = createCheckoutController(
                paymentMethod = paymentMethod,
                checkoutContext = checkoutContext,
            ),
        )

        if (newState != null) {
            uiState = newState
        }
    }

    private fun createCheckoutController(
        paymentMethod: PaymentMethod,
        checkoutContext: CheckoutContext.Sessions,
    ): CheckoutController {
        return CheckoutController(
            target = CheckoutTarget.PaymentMethod(paymentMethod.type),
            context = checkoutContext,
            callbacks = SessionCheckoutCallbacks(
                onError = ::onError,
                onFinished = ::onFinished,
            ),
            applicationContext = context,
            coroutineScope = viewModelScope,
        )
    }

    companion object {
        private val TAG = getLogTag()
    }
}
