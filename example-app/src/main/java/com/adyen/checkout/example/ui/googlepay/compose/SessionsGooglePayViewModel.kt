/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.ApiTaskResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class SessionsGooglePayViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel(), SessionComponentCallback<GooglePayComponentState>, ComponentAvailableCallback {

    private val checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig

    private val _googlePayState: MutableStateFlow<SessionsGooglePayState> =
        MutableStateFlow(SessionsGooglePayState.Loading)
    val googlePayState: StateFlow<SessionsGooglePayState> = _googlePayState.asStateFlow()

    private val _stateEvents: MutableStateFlow<SessionsGooglePayEvents> = MutableStateFlow(SessionsGooglePayEvents.None)
    val stateEvents: StateFlow<SessionsGooglePayEvents> = _stateEvents.asStateFlow()

    init {
        viewModelScope.launch { fetchSession() }
    }

    private suspend fun fetchSession() = withContext(Dispatchers.IO) {
        val paymentMethodType = PaymentMethodTypes.GOOGLE_PAY
        val checkoutSession = getSession(paymentMethodType)
        if (checkoutSession == null) {
            Log.e(TAG, "Failed to fetch session")
            onError()
            return@withContext
        }
        val paymentMethod = checkoutSession.getPaymentMethod(paymentMethodType)
        if (paymentMethod == null) {
            Log.e(TAG, "Session does not contain SCHEME payment method")
            onError()
            return@withContext
        }

        val componentData = SessionsGooglePayComponentData(
            checkoutSession,
            checkoutConfiguration,
            paymentMethod,
            this@SessionsGooglePayViewModel,
        )

        updateEvent { SessionsGooglePayEvents.ComponentData(componentData) }
        checkGooglePayAvailability(paymentMethod, checkoutConfiguration)
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
                redirectUrl = savedStateHandle.get<String>(SessionsGooglePayActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                allowedPaymentMethods = listOf(paymentMethodType),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
            ),
        ) ?: return null

        return getCheckoutSession(sessionModel, checkoutConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    private fun checkGooglePayAvailability(
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
    ) {
        GooglePayComponent.PROVIDER.isAvailable(
            application = application,
            paymentMethod = paymentMethod,
            checkoutConfiguration = checkoutConfiguration,
            callback = this,
        )
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            if (isAvailable) {
                updateState { SessionsGooglePayState.ShowButton }
            } else {
                onError()
            }
        }
    }

    override fun onAction(action: Action) {
        updateEvent { SessionsGooglePayEvents.Action(action) }
    }

    override fun onError(componentError: ComponentError) {
        Log.e(TAG, "Component error occurred")
        onError()
    }

    override fun onFinished(result: SessionPaymentResult) {
        updateState { SessionsGooglePayState.FinalResult(getFinalResultState(result)) }
    }

    private fun getFinalResultState(result: SessionPaymentResult): ResultState = when (result.resultCode) {
        "Authorised" -> ResultState.SUCCESS
        "Pending", "Received" -> ResultState.PENDING

        else -> ResultState.FAILURE
    }

    private fun onError() {
        updateState { SessionsGooglePayState.FinalResult(ResultState.FAILURE) }
    }

    private fun updateState(block: (SessionsGooglePayState) -> SessionsGooglePayState) {
        _googlePayState.update(block)
    }

    private fun updateEvent(block: (SessionsGooglePayEvents) -> SessionsGooglePayEvents) {
        _stateEvents.update(block)
    }

    fun onGooglePayLauncherResult(apiTaskResult: ApiTaskResult<PaymentData>) {
        // TODO Call Adyen API
    }

    fun onNewIntent(intent: Intent) {
        updateEvent { SessionsGooglePayEvents.Intent(intent) }
    }

    companion object {
        private val TAG = getLogTag()
    }
}
