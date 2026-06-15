/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.card
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.BeforeSubmitResult
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SessionCheckoutResult
import com.adyen.checkout.core.components.data.BeforeSubmitData
import com.adyen.checkout.core.components.data.ShopperName
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.OnBeforeSubmitMode
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class V6SessionsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel() {

    private lateinit var checkoutContext: CheckoutContext.Sessions
    private lateinit var sessionId: String
    private lateinit var sessionData: String

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
                payable = keyValueStorage.getOnBeforeSubmitMode() != OnBeforeSubmitMode.PATCH_SESSION_AMOUNT,
            ),
        ) ?: return

        val result = Checkout.setup(
            sessionResponse = sessionResponse,
            configuration = checkoutConfigurationProvider.checkoutConfig,
        )

        uiState = when (result) {
            is Checkout.Result.Error -> V6UiState.Error(UIText.String(result.error.message.orEmpty()))
            is Checkout.Result.Success -> {
                checkoutContext = result.checkoutContext
                sessionId = checkoutContext.checkoutSession.sessionSetupResponse.id
                sessionData = checkoutContext.checkoutSession.sessionSetupResponse.sessionData
                val paymentMethods = checkoutContext.getPaymentMethods()
                V6UiState.Component(
                    paymentMethods = paymentMethods,
                    storedPaymentMethods = checkoutContext.getStoredPaymentMethods(),
                    selectedPaymentMethod = paymentMethods.first(),
                    checkoutController = createCheckoutController(
                        paymentMethod = paymentMethods.first(),
                        checkoutContext = checkoutContext,
                    ),
                )
            }
        }
    }

    private fun onBinChange(binValue: String) {
        Log.d(TAG, "Bin value received: $binValue")
    }

    private fun onBinLookup(binLookupData: BinLookupData) {
        Log.d(TAG, "Bin Lookup Data received: $binLookupData")
    }

    private fun onFailure(error: CheckoutError) {
        Log.d(TAG, "onFailure: ${error.message}")
        uiState = V6UiState.Final(ResultState.FAILURE)
    }

    private fun onComplete(result: SessionCheckoutResult) {
        Log.d(TAG, "onComplete - Result code: ${result.resultCode}")
        uiState = V6UiState.Final(ResultState.get(result.resultCode.value))
    }

    @Suppress("unused")
    fun handleIntent(intent: Intent) {
        // TODO - Check if the controller should handle the intent or if we can do this inside a component
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethodResponse) {
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
        paymentMethod: PaymentMethodResponse,
        checkoutContext: CheckoutContext.Sessions,
    ): CheckoutController {
        val target = if (paymentMethod is StoredPaymentMethod) {
            CheckoutTarget.StoredPaymentMethod(paymentMethod.id)
        } else {
            CheckoutTarget.PaymentMethod(paymentMethod.type)
        }

        return CheckoutController(
            target = target,
            context = checkoutContext,
            callbacks = SessionCheckoutCallbacks(
                onFailure = ::onFailure,
                onComplete = ::onComplete,
                onBeforeSubmit = ::onBeforeSubmit,
            ) {
                card(
                    onBinChange = ::onBinChange,
                    onBinLookup = ::onBinLookup,
                )
            },
            coroutineScope = viewModelScope,
        )
    }

    private suspend fun onBeforeSubmit(data: BeforeSubmitData): BeforeSubmitResult {
        val mode = keyValueStorage.getOnBeforeSubmitMode()
        Log.d(TAG, "onBeforeSubmit called with mode: $mode")
        return when (mode) {
            OnBeforeSubmitMode.UPDATE_SHOPPER_DATA -> {
                BeforeSubmitResult.Proceed(
                    data = data.copy(
                        shopperEmail = "modified@example.com",
                        shopperName = ShopperName(
                            firstName = "John",
                            lastName = "Doe",
                        ),
                    ),
                )
            }

            OnBeforeSubmitMode.PATCH_SESSION_AMOUNT -> patchSession(data)
            OnBeforeSubmitMode.ABORT -> BeforeSubmitResult.Abort()
        }
    }

    private suspend fun patchSession(data: BeforeSubmitData): BeforeSubmitResult {
        val currentAmount = keyValueStorage.getAmount()
        val patchedAmount = currentAmount.copy(value = currentAmount.value + 100L)
        val patchResponse = paymentsRepository.patchSession(
            sessionId = sessionId,
            sessionData = sessionData,
            amount = patchedAmount,
        )
        return if (patchResponse != null) {
            sessionData = patchResponse.sessionData
            BeforeSubmitResult.Proceed(data, patchResponse.sessionData)
        } else {
            BeforeSubmitResult.Abort()
        }
    }

    companion object {
        private val TAG = getLogTag()
    }
}
