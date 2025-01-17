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
import com.adyen.checkout.components.core.internal.ui.model.updateFieldState
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.components.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.mbway.internal.ui.model.toViewState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.ui.model.SubmitHandlerEvent
import com.adyen.checkout.ui.core.internal.util.CountryUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

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

    private var state = MutableStateFlow(MBWayDelegateState())

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<MBWayComponentState> = _componentStateFlow

    override val viewStateFlow: Flow<MBWayViewState> by lazy {
        state.map(
            MBWayDelegateState::toViewState,
        ).stateIn(coroutineScope, SharingStarted.Lazily, state.value.toViewState())
    }

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(MbWayComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<MBWayComponentState> = getTrackedSubmitFlow()

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    init {
        updateComponentState()
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope

        initializeSubmitHandler(coroutineScope)
        initializeAnalytics(coroutineScope)
    }

    private fun initializeSubmitHandler(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        submitHandler.submitEventFlow
            .onEach { submitEvent ->
                when (submitEvent) {
                    // TODO: Focus on the first invalid field (can be in UI?)
                    SubmitHandlerEvent.InvalidInput -> validateAllFields()
                }
            }.launchIn(coroutineScope)
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

    private fun validateAllFields() {
        MBWayFieldId.entries.forEach { fieldId ->
            val value = when (fieldId) {
                MBWayFieldId.COUNTRY_CODE -> state.value.countryCodeFieldState.value
                MBWayFieldId.LOCAL_PHONE_NUMBER -> state.value.localPhoneNumberFieldState.value
            }
            // TODO: Update only the fields which are not validated yet.
            updateField(fieldId, value = value)
        }
        state.update { state ->
            state.copy(showValidationErrorsRegardlessFocus = true)
        }
    }

    private fun updateField(
        fieldId: MBWayFieldId,
        value: String? = null,
        hasFocus: Boolean? = null,
    ) {
        state.update { state ->
            val updatedState = when (fieldId) {
                MBWayFieldId.COUNTRY_CODE -> state.copy(
                    countryCodeFieldState = state.countryCodeFieldState.updateFieldState(
                        value = value,
                        // TODO: This value manipulation should move somewhere else
                        validation = value?.let { validationRegistry.validate(fieldId, it.trimStart('0')) },
                        hasFocus = hasFocus,
                    ),
                )

                MBWayFieldId.LOCAL_PHONE_NUMBER -> state.copy(
                    localPhoneNumberFieldState = state.localPhoneNumberFieldState.updateFieldState(
                        value = value,
                        validation = value?.let { validationRegistry.validate(fieldId, it) },
                        hasFocus = hasFocus,
                    ),
                )
            }

            // Resetting the showValidationErrorsRegardlessFocus flag, to hide error when focused on a field
            updatedState.copy(
                showValidationErrorsRegardlessFocus = false,
            )
        }
    }

    override fun onFieldValueChanged(fieldId: MBWayFieldId, value: String) {
        updateField(fieldId, value = value)
        onDataChanged()
    }

    override fun onFieldFocusChanged(fieldId: MBWayFieldId, hasFocus: Boolean) =
        updateField(fieldId, hasFocus = hasFocus)

    private fun onDataChanged() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onDataChanged" }
        updateComponentState()
    }

    // TODO: This has to be done regardless
    @VisibleForTesting
    internal fun updateComponentState() {
        val componentState = createComponentState()
        componentStateChanged(componentState)
    }

    private fun createComponentState(): MBWayComponentState {
        val paymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            // TODO: This value manipulation should move somewhere else
            telephoneNumber = state.value.localPhoneNumberFieldState.value.trimStart('0'),
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return MBWayComponentState(
            data = paymentComponentData,
            isInputValid = state.value.isValid,
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
