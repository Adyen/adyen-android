/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/1/2023.
 */

package com.adyen.checkout.example.ui.card

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.exception.CancellationException
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.card.compose.SessionsCardActivity
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class SessionsCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel(), SessionComponentCallback<CardComponentState> {

    private val cardConfiguration = checkoutConfigurationProvider.getCardConfiguration()

    private val _uiState = MutableStateFlow(SessionsCardUiState(cardConfiguration))
    val uiState: StateFlow<SessionsCardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { launchComponent() }
    }

    private suspend fun launchComponent() {
        updateUiState { it.copy(isLoading = true) }
        val paymentMethodType = PaymentMethodTypes.SCHEME
        val checkoutSession = getSession(paymentMethodType)
        if (checkoutSession == null) {
            Log.e(TAG, "Failed to fetch session")
            onError("Failed to fetch session")
            return
        }
        val paymentMethod = checkoutSession.getPaymentMethod(paymentMethodType)
        if (paymentMethod == null) {
            Log.e(TAG, "Session does not contain SCHEME payment method")
            onError("Payment method is null")
            return
        }

        val componentData = SessionsCardComponentData(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            callback = this,
        )
        updateUiState { it.copy(componentData = componentData, isLoading = false) }
    }

    private suspend fun getSession(paymentMethodType: String): CheckoutSession? {
        val sessionModel = paymentsRepository.createSession(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enabled(),
                redirectUrl = savedStateHandle.get<String>(SessionsCardActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                allowedPaymentMethods = listOf(paymentMethodType),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
            ),
        ) ?: return null

        return getCheckoutSession(sessionModel, cardConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        cardConfiguration: CardConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, cardConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    override fun onAction(action: Action) {
        updateUiState { it.copy(action = action) }
    }

    override fun onError(componentError: ComponentError) {
        if (componentError.exception is CancellationException) {
            updateUiState {
                it.copy(
                    toastMessage = "Payment in progress was cancelled",
                    finalResult = ResultState.FAILURE,
                )
            }
        } else {
            onError(componentError.errorMessage)
        }
    }

    private fun onError(message: String) {
        updateUiState { it.copy(toastMessage = "Error: $message") }
    }

    override fun onFinished(result: SessionPaymentResult) {
        updateUiState {
            it.copy(
                toastMessage = "Finished: ${result.resultCode}",
                finalResult = getFinalResultState(result),
            )
        }
    }

    private fun getFinalResultState(result: SessionPaymentResult): ResultState = when (result.resultCode) {
        "Authorised" -> ResultState.SUCCESS
        "Pending",
        "Received" -> ResultState.PENDING

        else -> ResultState.FAILURE
    }

    override fun onLoading(isLoading: Boolean) {
        updateUiState { it.copy(isLoading = isLoading) }
    }

    fun toastMessageConsumed() {
        updateUiState { it.copy(toastMessage = null) }
    }

    fun actionConsumed() {
        updateUiState { it.copy(action = null) }
    }

    private fun updateUiState(block: (SessionsCardUiState) -> SessionsCardUiState) {
        _uiState.update(block)
    }

    companion object {
        private val TAG = getLogTag()
    }
}
