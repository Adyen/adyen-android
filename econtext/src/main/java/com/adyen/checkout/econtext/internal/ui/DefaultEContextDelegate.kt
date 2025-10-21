/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/1/2023.
 */

package com.adyen.checkout.econtext.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.components.core.paymentmethod.EContextPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.econtext.R
import com.adyen.checkout.econtext.internal.ui.model.EContextInputData
import com.adyen.checkout.econtext.internal.ui.model.EContextOutputData
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
import java.util.Locale

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultEContextDelegate<
    EContextPaymentMethodT : EContextPaymentMethod,
    EContextComponentStateT : PaymentComponentState<EContextPaymentMethodT>
    >(
    private val observerRepository: PaymentObserverRepository,
    override val componentParams: ButtonComponentParams,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<EContextComponentStateT>,
    private val typedPaymentMethodFactory: () -> EContextPaymentMethodT,
    private val componentStateFactory: (
        data: PaymentComponentData<EContextPaymentMethodT>,
        isInputValid: Boolean,
        isReady: Boolean
    ) -> EContextComponentStateT,
    private val sdkDataProvider: SdkDataProvider,
) : EContextDelegate<EContextPaymentMethodT, EContextComponentStateT> {

    private val inputData: EContextInputData = EContextInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<EContextOutputData> = _outputDataFlow

    override val outputData: EContextOutputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<EContextComponentStateT> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(EContextComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<EContextComponentStateT> = getTrackedSubmitFlow()
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun updateInputData(update: EContextInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData(): EContextOutputData {
        return EContextOutputData(
            firstNameState = validateFirstName(inputData.firstName),
            lastNameState = validateLastName(inputData.lastName),
            phoneNumberState = validatePhoneNumber(inputData.mobileNumber, inputData.countryCode),
            emailAddressState = validateEmailAddress(inputData.emailAddress),
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: EContextOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: EContextOutputData = this.outputData
    ): EContextComponentStateT {
        val eContextPaymentMethod = typedPaymentMethodFactory().apply {
            type = getPaymentMethodType()
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId()
            firstName = outputData.firstNameState.value
            lastName = outputData.lastNameState.value
            telephoneNumber = outputData.phoneNumberState.value
            shopperEmail = outputData.emailAddressState.value
        }
        val isInputValid = outputData.isValid
        val paymentComponentData = PaymentComponentData(
            paymentMethod = eContextPaymentMethod,
            order = order,
            amount = componentParams.amount,
            sdkData = sdkDataProvider.createEncodedSdkData(),
        )
        return componentStateFactory(paymentComponentData, isInputValid, true)
    }

    private fun validateFirstName(firstName: String): FieldState<String> {
        val validation = if (firstName.isNotBlank()) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_econtext_first_name_invalid)
        }
        return FieldState(firstName, validation)
    }

    private fun validateLastName(lastName: String): FieldState<String> {
        val validation = if (lastName.isNotBlank()) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_econtext_last_name_invalid)
        }
        return FieldState(lastName, validation)
    }

    private fun validatePhoneNumber(phoneNumber: String, countryCode: String): FieldState<String> {
        val sanitizedNumber = phoneNumber.trimStart('0')
        val fullPhoneNumber = countryCode + sanitizedNumber
        val validation = if (fullPhoneNumber.isNotEmpty() && ValidationUtils.isPhoneNumberValid(fullPhoneNumber)) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_econtext_phone_number_invalid)
        }
        return FieldState(fullPhoneNumber, validation)
    }

    private fun validateEmailAddress(emailAddress: String): FieldState<String> {
        val validation = if (emailAddress.isNotEmpty() && ValidationUtils.isEmailValid(emailAddress)) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_econtext_shopper_email_invalid)
        }
        return FieldState(emailAddress, validation)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<EContextComponentStateT>) -> Unit
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

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        val state = _componentStateFlow.value
        submitHandler.onSubmit(state)
    }

    override fun getSupportedCountries(): List<CountryModel> =
        CountryUtils.getLocalizedCountries(componentParams.shopperLocale)

    override fun getInitiallySelectedCountry(): CountryModel? {
        val countries = getSupportedCountries()
        return countries.firstOrNull { it.isoCode == Locale.JAPAN.country } ?: countries.firstOrNull()
    }

    override fun isConfirmationRequired(): Boolean {
        return _viewFlow.value is ButtonComponentViewType
    }

    override fun shouldShowSubmitButton(): Boolean {
        return isConfirmationRequired() && componentParams.isSubmitButtonVisible
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }
}
