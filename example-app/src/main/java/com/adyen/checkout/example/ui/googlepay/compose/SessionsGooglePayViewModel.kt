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
import com.adyen.checkout.example.ui.googlepay.GooglePayActivityResult
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
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
) : ViewModel(),
    SessionComponentCallback<GooglePayComponentState>,
    ComponentAvailableCallback {

    private val googlePayConfiguration = checkoutConfigurationProvider.getGooglePayConfiguration()

    private val _googlePayState = MutableStateFlow(SessionsGooglePayState(SessionsGooglePayUIState.Loading))
    val googlePayState: StateFlow<SessionsGooglePayState> = _googlePayState.asStateFlow()

    private var _componentData: SessionsGooglePayComponentData? = null
    private val componentData: SessionsGooglePayComponentData
        get() = requireNotNull(_componentData) { "component data should not be null" }

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

        _componentData = SessionsGooglePayComponentData(
            checkoutSession,
            googlePayConfiguration,
            paymentMethod,
            this@SessionsGooglePayViewModel,
        )

        checkGooglePayAvailability(paymentMethod, googlePayConfiguration)
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

        return getCheckoutSession(sessionModel, googlePayConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        googlePayConfiguration: GooglePayConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, googlePayConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    private fun checkGooglePayAvailability(
        paymentMethod: PaymentMethod,
        googlePayConfiguration: GooglePayConfiguration,
    ) {
        GooglePayComponent.PROVIDER.isAvailable(
            application,
            paymentMethod,
            googlePayConfiguration,
            this,
        )
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            if (isAvailable) {
                updateState { it.copy(uiState = SessionsGooglePayUIState.ShowButton(componentData)) }
            } else {
                onError()
            }
        }
    }

    override fun onAction(action: Action) {
        updateState { it.copy(actionToHandle = SessionsGooglePayAction(componentData, action)) }
    }

    override fun onError(componentError: ComponentError) {
        Log.e(TAG, "Component error occurred")
        onError()
    }

    override fun onFinished(result: SessionPaymentResult) {
        updateState {
            it.copy(uiState = SessionsGooglePayUIState.FinalResult(getFinalResultState(result)))
        }
    }

    private fun getFinalResultState(result: SessionPaymentResult): ResultState = when (result.resultCode) {
        "Authorised" -> ResultState.SUCCESS
        "Pending",
        "Received" -> ResultState.PENDING

        else -> ResultState.FAILURE
    }

    private fun onError() {
        updateState { it.copy(uiState = SessionsGooglePayUIState.FinalResult(ResultState.FAILURE)) }
    }

    private fun updateState(block: (SessionsGooglePayState) -> SessionsGooglePayState) {
        _googlePayState.update(block)
    }

    fun onButtonClicked() {
        updateState {
            it.copy(
                uiState = SessionsGooglePayUIState.ShowComponent(componentData),
                startGooglePay = SessionsStartGooglePayData(componentData, ACTIVITY_RESULT_CODE),
            )
        }
    }

    fun onGooglePayStarted() {
        updateState { it.copy(startGooglePay = null) }
    }

    fun onActionConsumed() {
        updateState { it.copy(actionToHandle = null) }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != ACTIVITY_RESULT_CODE) return
        updateState { it.copy(activityResultToHandle = GooglePayActivityResult(componentData, resultCode, data)) }
    }

    fun onActivityResultHandled() {
        updateState { it.copy(activityResultToHandle = null) }
    }

    fun onNewIntent(intent: Intent) {
        updateState { it.copy(intentToHandle = SessionsGooglePayIntent(componentData, intent)) }
    }

    fun onNewIntentHandled() {
        updateState { it.copy(intentToHandle = null) }
    }

    companion object {
        private val TAG = getLogTag()
        private const val ACTIVITY_RESULT_CODE = 1
    }
}
