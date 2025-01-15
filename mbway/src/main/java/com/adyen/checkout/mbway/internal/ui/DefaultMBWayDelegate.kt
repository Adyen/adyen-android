/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldState
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.components.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.util.CountryUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class DefaultMBWayDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<MBWayComponentState>,
    private val validationRegistry: FieldValidatorRegistry<MBWayFieldId>,
) : MBWayDelegate {

    private var localPhoneNumber = ComponentFieldState("")
    private var countryCode = ComponentFieldState("")

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<MBWayComponentState> = _componentStateFlow

    private val _viewStateFlow: MutableStateFlow<MBWayViewState> = MutableStateFlow(MBWayViewState())
    override val viewStateFlow: Flow<MBWayViewState> = _viewStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(MbWayComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<MBWayComponentState> = getTrackedSubmitFlow()

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    init {
        updateComponentState()
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val renderedEvent = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(renderedEvent)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<MBWayComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onFieldValueChanged(fieldId: MBWayFieldId, value: String) {
        when (fieldId) {
            MBWayFieldId.COUNTRY_CODE -> updateCountryCodeValue(value)
            MBWayFieldId.LOCAL_PHONE_NUMBER -> updateLocalPhoneNumberValue(value)
        }

        onDataChanged()
    }

    // TODO: Validation should not be done here, only when focus changes?
    // TODO: Think about the submit button click also. We need to do the validation then.
    private fun updateCountryCodeValue(value: String) {
        val validation = validationRegistry.validate(MBWayFieldId.COUNTRY_CODE, value.trimStart('0'))

        countryCode = countryCode.copy(
            value = value,
            validation = validation,
        )
    }

    // TODO: Validation should not be done here, only when focus changes?
    private fun updateLocalPhoneNumberValue(value: String) {
        val validation = validationRegistry.validate(MBWayFieldId.LOCAL_PHONE_NUMBER, value)

        localPhoneNumber = localPhoneNumber.copy(
            value = value,
            validation = validation,
        )
    }

    private fun onDataChanged() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onDataChanged" }
        updateViewState()
        updateComponentState()
    }

    private fun updateViewState() {
        _viewStateFlow.value = _viewStateFlow.value.copy(phoneNumberFieldState = localPhoneNumber)
    }

    @VisibleForTesting
    internal fun updateComponentState() {
        val componentState = createComponentState()
        componentStateChanged(componentState)
    }

    private fun createComponentState(): MBWayComponentState {
        val paymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            // TODO: These value manipulation should move somewhere else
            telephoneNumber = localPhoneNumber.value.trimStart('0'),
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return MBWayComponentState(
            data = paymentComponentData,
            isInputValid = localPhoneNumber.validation?.isValid() ?: false,
            isReady = true,
        )
    }

    private fun componentStateChanged(componentState: MBWayComponentState) {
        _componentStateFlow.tryEmit(componentState)
    }

    override fun getSupportedCountries(): List<CountryModel> =
        CountryUtils.getLocalizedCountries(componentParams.shopperLocale, SUPPORTED_COUNTRIES)

    override fun getInitiallySelectedCountry(): CountryModel? {
        val countries = getSupportedCountries()
        return countries.firstOrNull { it.isoCode == ISO_CODE_PORTUGAL } ?: countries.firstOrNull()
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }

    companion object {
        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"

        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
