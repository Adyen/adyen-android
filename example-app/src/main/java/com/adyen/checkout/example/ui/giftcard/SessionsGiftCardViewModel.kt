/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/4/2023.
 */

package com.adyen.checkout.example.ui.giftcard

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.SessionsGiftCardComponentCallback
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
internal class SessionsGiftCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel(), SessionsGiftCardComponentCallback {

    private val _giftCardComponentDataFlow = MutableStateFlow<SessionsGiftCardComponentData?>(null)
    val giftCardComponentDataFlow: Flow<SessionsGiftCardComponentData> = _giftCardComponentDataFlow.filterNotNull()

    private val _giftCardViewStateFlow = MutableStateFlow<GiftCardViewState>(GiftCardViewState.Loading)
    internal val giftCardViewStateFlow: Flow<GiftCardViewState> = _giftCardViewStateFlow

    private val _events = MutableSharedFlow<GiftCardEvent>()
    internal val events: Flow<GiftCardEvent> = _events

    private var currentSession: CheckoutSession? = null

    init {
        viewModelScope.launch { launchComponent() }
    }

    private suspend fun launchComponent() {
        val paymentMethodType = PaymentMethodTypes.GIFTCARD
        val checkoutSession = getSession(paymentMethodType)
        if (checkoutSession == null) {
            Log.e(TAG, "Failed to fetch session")
            _giftCardViewStateFlow.emit(GiftCardViewState.Error)
            return
        }
        val giftCardPaymentMethod = checkoutSession.getPaymentMethod(paymentMethodType)

        if (giftCardPaymentMethod == null) {
            _giftCardViewStateFlow.emit(GiftCardViewState.Error)
        } else {
            _giftCardComponentDataFlow.emit(
                SessionsGiftCardComponentData(
                    checkoutSession = checkoutSession,
                    paymentMethod = giftCardPaymentMethod,
                    callback = this@SessionsGiftCardViewModel,
                ),
            )
            _giftCardViewStateFlow.emit(GiftCardViewState.ShowComponent)
        }
    }

    private suspend fun getSession(paymentMethodType: String): CheckoutSession? {
        val sessionModel = paymentsRepository.createSessionOld(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                redirectUrl = savedStateHandle.get<String>(SessionsGiftCardActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                allowedPaymentMethods = listOf(paymentMethodType),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
                showRemovePaymentMethodButton = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
            ),
        ) ?: return null

        return getCheckoutSession(sessionModel)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        order: Order? = null,
    ): CheckoutSession? {
        return when (
            val result = CheckoutSessionProvider.createSession(
                sessionModel = sessionModel,
                configuration = checkoutConfigurationProvider.checkoutConfig,
                order = order,
            )
        ) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    private suspend fun getCheckoutSession(
        sessionPaymentResult: SessionPaymentResult
    ): CheckoutSession? {
        return when (
            val result = CheckoutSessionProvider.createSession(
                sessionPaymentResult = sessionPaymentResult,
                configuration = checkoutConfigurationProvider.checkoutConfig,
            )
        ) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    override fun onAction(action: Action) {
        _events.tryEmit(GiftCardEvent.AdditionalAction(action))
    }

    override fun onError(componentError: ComponentError) {
        _events.tryEmit(GiftCardEvent.PaymentResult("Failed: ${componentError.errorMessage}"))
    }

    override fun onFinished(result: SessionPaymentResult) {
        viewModelScope.launch {
            _events.emit(GiftCardEvent.PaymentResult(result.resultCode.orEmpty()))
        }
    }

    // no ops
    override fun onStateChanged(state: GiftCardComponentState) = Unit

    override fun onPartialPayment(result: SessionPaymentResult) {
        viewModelScope.launch {
            currentSession = getCheckoutSession(result)
            _events.emit(GiftCardEvent.PaymentResult(result.resultCode.orEmpty()))
        }
    }

    fun reloadComponentWithOrder() {
        val giftCardComponentData = _giftCardComponentDataFlow.value
        val currentSession = this.currentSession
        if (giftCardComponentData != null && currentSession != null) {
            viewModelScope.launch {
                _events.emit(
                    GiftCardEvent.ReloadComponentSessions(
                        giftCardComponentData.copy(
                            checkoutSession = currentSession,
                        ),
                    ),
                )
            }
        }
    }

    companion object {
        private val TAG = getLogTag()
    }
}
