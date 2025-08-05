/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutContext
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.ComponentError
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class V6ViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel() {

    // TODO - Replace with checkoutConfigurationProvider once it's updated COSDK-563
    private val configuration = CheckoutConfiguration(
        Environment.TEST,
        BuildConfig.CLIENT_KEY,
    )

    var checkoutContext by mutableStateOf<CheckoutContext?>(null)

    init {
        viewModelScope.launch {
            fetchPaymentMethods()
        }
    }

    private suspend fun fetchPaymentMethods() {
        val paymentMethodResponse = paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )

        if (paymentMethodResponse == null) {
            // TODO - Example app error handling
            Log.d(TAG, "Payment Method Response is null.")
            return
        }

        val result = Checkout.initialize(
            paymentMethodsApiResponse = paymentMethodResponse,
            checkoutConfiguration = configuration,
            checkoutCallbacks = CheckoutCallbacks(
                onSubmit = ::onSubmit,
                onAdditionalDetails = ::onAdditionalDetails,
                onError = ::onError,
            ),
        )

        checkoutContext = when (result) {
            is Checkout.Result.Error -> null
            is Checkout.Result.Success -> result.checkoutContext
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        // TODO - make payments call
        return CheckoutResult.Finished()
    }

    @Suppress("UnusedParameter")
    private fun onAdditionalDetails(actionComponentData: ActionComponentData): CheckoutResult {
        // TODO - make payments details call
        return CheckoutResult.Finished()
    }

    @Suppress("UnusedParameter")
    private fun onError(componentError: ComponentError) {
        // TODO - handle error
    }

    companion object {
        private val TAG = getLogTag()
    }
}
