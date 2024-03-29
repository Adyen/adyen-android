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
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.ACHDirectDebitPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@Suppress("TooManyFunctions")
internal class StoredACHDirectDebitDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsManager: AnalyticsManager,
    override val componentParams: ACHDirectDebitComponentParams,
    private val order: OrderRequest?,
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

    private val submitChannel = bufferedChannel<ACHDirectDebitComponentState>()
    override val submitFlow: Flow<ACHDirectDebitComponentState> = submitChannel.receiveAsFlow()

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
        initializeAnalytics(coroutineScope)

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "setupAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(
            component = storedPaymentMethod.type.orEmpty(),
            isStoredPaymentMethod = true
        )
        analyticsManager.trackEvent(event)
    }

    private fun onState(achDirectDebitComponentState: ACHDirectDebitComponentState) {
        if (achDirectDebitComponentState.isValid) {
            val event = GenericEvents.submit(storedPaymentMethod.type.orEmpty())
            analyticsManager.trackEvent(event)

            submitChannel.trySend(achDirectDebitComponentState)
        }
    }

    override fun updateInputData(update: ACHDirectDebitInputData.() -> Unit) {
        adyenLog(AdyenLogLevel.ERROR) { "updateInputData should not be called in StoredACHDirectDebitDelegate" }
    }

    @Suppress("ForbiddenComment")
    // TODO: Here we only call this method on initialization. The checkoutAttemptId will only be available if it is
    //  passed by drop-in. This should be fixed as part of state refactoring.
    private fun createComponentState(): ACHDirectDebitComponentState {
        val paymentMethod = ACHDirectDebitPaymentMethod(
            type = ACHDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            storedPaymentMethodId = storedPaymentMethod.id,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return ACHDirectDebitComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    private fun createOutputData() = with(inputData) {
        ACHDirectDebitOutputData(
            bankAccountNumber = FieldState(bankAccountNumber, Validation.Valid),
            bankLocationId = FieldState(bankLocationId, Validation.Valid),
            ownerName = FieldState(ownerName, Validation.Valid),
            addressState = AddressValidationUtils.makeValidEmptyAddressOutput(inputData.address),
            addressUIState = AddressFormUIState.NONE,
            shouldStorePaymentMethod = isStorePaymentMethodSwitchChecked,
            showStorePaymentField = false,
        )
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
        analyticsManager.clear(this)
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
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        adyenLog(AdyenLogLevel.ERROR) { "updateAddressInputData should not be called in StoredACHDirectDebitDelegate" }
    }
}
