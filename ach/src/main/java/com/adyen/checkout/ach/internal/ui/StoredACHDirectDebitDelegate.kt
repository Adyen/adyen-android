/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/2/2023.
 */

package com.adyen.checkout.ach.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.ach.ACHDirectDebitComponentState
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitComponentParams
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitInputData
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitOutputData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.ACHDirectDebitPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class StoredACHDirectDebitDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsRepository: AnalyticsRepository,
    override val componentParams: ACHDirectDebitComponentParams,
    private val order: OrderRequest?,
    private val submitHandler: SubmitHandler<ACHDirectDebitComponentState>,
) : ACHDirectDebitDelegate {

    private val inputData: ACHDirectDebitInputData = ACHDirectDebitInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<ACHDirectDebitOutputData> = _outputDataFlow

    override val outputData: ACHDirectDebitOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<ACHDirectDebitComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow
    override val submitFlow: Flow<ACHDirectDebitComponentState> = submitHandler.submitFlow

    override val addressOutputData: AddressOutputData
        get() = outputData.addressState

    override val addressOutputDataFlow: Flow<AddressOutputData> by lazy {
        outputDataFlow.map {
            it.addressState
        }.stateIn(coroutineScope, SharingStarted.Lazily, outputData.addressState)
    }

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        submitHandler.initialize(coroutineScope, componentStateFlow)
        sendAnalyticsEvent(coroutineScope)
    }

    override fun updateInputData(update: ACHDirectDebitInputData.() -> Unit) {
        Logger.e(TAG, "updateInputData should not be called in StoredACHDirectDebitDelegate")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    private fun sendAnalyticsEvent(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "sendAnalyticsEvent")
        coroutineScope.launch {
            analyticsRepository.sendAnalyticsEvent()
        }
    }

    private fun createComponentState(): ACHDirectDebitComponentState {
        val paymentMethod = ACHDirectDebitPaymentMethod(
            type = ACHDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE,
            storedPaymentMethodId = storedPaymentMethod.id
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
        )

        return ACHDirectDebitComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true
        )
    }

    private fun createOutputData() = with(inputData) {
        ACHDirectDebitOutputData(
            bankAccountNumber = FieldState(bankAccountNumber, Validation.Valid),
            bankLocationId = FieldState(bankLocationId, Validation.Valid),
            ownerName = FieldState(ownerName, Validation.Valid),
            addressState = AddressValidationUtils.makeValidEmptyAddressOutput(inputData.address),
            addressUIState = AddressFormUIState.NONE,
            isStoredPaymentMethodEnabled = isStorePaymentSelected,
            showStorePaymentField = false,
        )
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
    }

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<ACHDirectDebitComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = exceptionFlow,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        Logger.e(TAG, "updateAddressInputData should not be called in StoredACHDirectDebitDelegate")
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(
            state = state
        )
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
