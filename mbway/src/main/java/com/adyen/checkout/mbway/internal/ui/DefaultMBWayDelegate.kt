/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.updateFieldState
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.mbway.internal.ui.model.toComponentState
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
import kotlinx.coroutines.flow.StateFlow
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
    private val transformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    private val validationRegistry: FieldValidatorRegistry<MBWayFieldId>,
) : MBWayDelegate {

    private val state = MutableStateFlow(
        MBWayDelegateState(
            countries = getSupportedCountries(),
            countryCodeFieldState = ComponentFieldDelegateState(getInitiallySelectedCountry()),
        ),
    )

    override val componentStateFlow: StateFlow<MBWayComponentState> by lazy {
        val toComponentState: (MBWayDelegateState) -> MBWayComponentState = { delegateState ->
            delegateState.toComponentState(
                analyticsManager,
                transformerRegistry,
                order,
                componentParams.amount,
            )
        }
        state
            .map(toComponentState)
            .stateIn(coroutineScope, SharingStarted.Lazily, toComponentState(state.value))
    }

    override val viewStateFlow: Flow<MBWayViewState> by lazy {
        state
            .map(MBWayDelegateState::toViewState)
            .stateIn(coroutineScope, SharingStarted.Lazily, state.value.toViewState())
    }

    private val _viewFlow: MutableStateFlow<ComponentViewType?> =
        MutableStateFlow(MbWayComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<MBWayComponentState> = getTrackedSubmitFlow()

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

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
        // Flag to focus only the first invalid field
        var isErrorFocused = false

        MBWayFieldId.entries.forEach { fieldId ->
            val fieldState = when (fieldId) {
                MBWayFieldId.COUNTRY_CODE -> state.value.countryCodeFieldState
                MBWayFieldId.LOCAL_PHONE_NUMBER -> state.value.localPhoneNumberFieldState
            }

            val shouldFocus = !isErrorFocused && fieldState.validation is Validation.Invalid
            if (shouldFocus) {
                isErrorFocused = true
            }

            updateField(
                fieldId = fieldId,
                value = fieldState.value, // Ensure the current value is validated
                hasFocus = shouldFocus,
                isValidationErrorCheckForced = true,
            )
        }
    }

    private fun <T> updateField(
        fieldId: MBWayFieldId,
        value: T? = null,
        hasFocus: Boolean? = null,
        // By default this flag will be false, to hide validation errors when the field gets updated
        isValidationErrorCheckForced: Boolean = false,
    ) {
        val validation = value?.let {
            validationRegistry.validate(
                fieldId,
                transformerRegistry.transform(fieldId, value),
            )
        }

        state.update { state ->
            when (fieldId) {
                MBWayFieldId.COUNTRY_CODE -> {
                    state.copy(
                        countryCodeFieldState = state.countryCodeFieldState.updateFieldState(
                            value = value as? CountryModel,
                            validation = validation,
                            hasFocus = hasFocus,
                            isValidationErrorCheckForced = isValidationErrorCheckForced,
                        ),
                    )
                }

                MBWayFieldId.LOCAL_PHONE_NUMBER -> {
                    state.copy(
                        localPhoneNumberFieldState = state.localPhoneNumberFieldState.updateFieldState(
                            value = value as? String,
                            validation = validation,
                            hasFocus = hasFocus,
                            isValidationErrorCheckForced = isValidationErrorCheckForced,
                        ),
                    )
                }
            }
        }
    }

    override fun onFieldValueChanged(fieldId: MBWayFieldId, value: String) =
        updateField(fieldId, value = value)

    override fun onFieldFocusChanged(fieldId: MBWayFieldId, hasFocus: Boolean) = updateField<Unit>(fieldId, hasFocus = hasFocus)

    private fun getSupportedCountries(): List<CountryModel> =
        CountryUtils.getLocalizedCountries(componentParams.shopperLocale, SUPPORTED_COUNTRIES)

    private fun getInitiallySelectedCountry(): CountryModel {
        val countries = getSupportedCountries()
        return countries.firstOrNull { it.isoCode == ISO_CODE_PORTUGAL } ?: countries.firstOrNull()
        ?: throw IllegalArgumentException("Countries list can not be null")
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        submitHandler.onSubmit(componentStateFlow.value)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean =
        isConfirmationRequired() && componentParams.isSubmitButtonVisible

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
