/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/6/2023.
 */

package com.adyen.checkout.atome.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.atome.AtomeComponentState
import com.adyen.checkout.atome.R
import com.adyen.checkout.atome.internal.ui.model.AtomeComponentParams
import com.adyen.checkout.atome.internal.ui.model.AtomeInputData
import com.adyen.checkout.atome.internal.ui.model.AtomeOutputData
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.components.core.internal.util.isEmpty
import com.adyen.checkout.components.core.paymentmethod.AtomePaymentMethod
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.data.api.AddressRepository
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultAtomeDelegate(
    private val submitHandler: SubmitHandler<AtomeComponentState>,
    private val analyticsRepository: AnalyticsRepository,
    private val observerRepository: PaymentObserverRepository,
    private val addressRepository: AddressRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: AtomeComponentParams,
) : AtomeDelegate {

    private val inputData = AtomeInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<AtomeOutputData> = _outputDataFlow

    override val outputData: AtomeOutputData get() = _outputDataFlow.value

    override val addressOutputData: AddressOutputData
        get() = outputData.billingAddressState

    override val addressOutputDataFlow: Flow<AddressOutputData> by lazy {
        outputDataFlow.map {
            it.billingAddressState
        }.stateIn(coroutineScope, SharingStarted.Lazily, outputData.billingAddressState)
    }

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<AtomeComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(AtomeComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<AtomeComponentState> = submitHandler.submitFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        submitHandler.initialize(coroutineScope, componentStateFlow)
        sendAnalyticsEvent(coroutineScope)

        subscribeToStatesList()
        subscribeToCountryList()
        requestCountryList()
    }

    private fun subscribeToCountryList() {
        addressRepository.countriesFlow
            .distinctUntilChanged()
            .onEach { countries ->
                Logger.d(TAG, "New countries emitted - countries: ${countries.size}")
                val countryOptions = AddressFormUtils.initializeCountryOptions(
                    shopperLocale = componentParams.shopperLocale,
                    addressParams = componentParams.addressParams,
                    countryList = countries
                )
                countryOptions.firstOrNull { it.selected }?.let {
                    inputData.billingAddress.country = it.code
                    requestStateList(it.code)
                }
                updateOutputData(countryOptions = countryOptions)
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToStatesList() {
        addressRepository.statesFlow
            .distinctUntilChanged()
            .onEach { states ->
                Logger.d(TAG, "New states emitted - states: ${states.size}")
                updateOutputData(stateOptions = AddressFormUtils.initializeStateOptions(states))
            }
            .launchIn(coroutineScope)
    }

    private fun requestCountryList() {
        addressRepository.getCountryList(
            shopperLocale = componentParams.shopperLocale,
            coroutineScope = coroutineScope
        )
    }

    private fun requestStateList(countryCode: String?) {
        addressRepository.getStateList(
            shopperLocale = componentParams.shopperLocale,
            countryCode = countryCode,
            coroutineScope = coroutineScope
        )
    }

    private fun sendAnalyticsEvent(coroutineScope: CoroutineScope) {
        Logger.v(TAG, "sendAnalyticsEvent")
        coroutineScope.launch {
            analyticsRepository.sendAnalyticsEvent()
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<AtomeComponentState>) -> Unit
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

    override fun updateInputData(update: AtomeInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        updateInputData {
            this.billingAddress.update()
        }
    }

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")
        updateOutputData()
    }

    private fun createOutputData(
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
    ): AtomeOutputData = with(inputData) {
        AtomeOutputData(
            firstNameState = validateFirstName(firstName),
            lastNameState = validateLastName(lastName),
            phoneNumberState = validatePhoneNumber(mobileNumber, countryCode),
            billingAddressState = validateAddress(billingAddress, countryOptions, stateOptions)
        )
    }

    private fun updateOutputData(
        countryOptions: List<AddressListItem> = outputData.billingAddressState.countryOptions,
        stateOptions: List<AddressListItem> = outputData.billingAddressState.stateOptions,
    ) {
        val newOutputData = createOutputData(countryOptions, stateOptions)
        outputDataChanged(newOutputData)
        updateComponentState(newOutputData)
    }

    private fun outputDataChanged(outputData: AtomeOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    private fun validateFirstName(firstName: String): FieldState<String> {
        val validation = if (firstName.isNotBlank()) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_atome_first_name_invalid)
        }
        return FieldState(firstName, validation)
    }

    private fun validateLastName(lastName: String): FieldState<String> {
        val validation = if (lastName.isNotBlank()) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_atome_last_name_invalid)
        }
        return FieldState(lastName, validation)
    }

    private fun validatePhoneNumber(phoneNumber: String, countryCode: String): FieldState<String> {
        val sanitizedNumber = phoneNumber.trimStart('0')
        val fullPhoneNumber = countryCode + sanitizedNumber
        val validation = if (fullPhoneNumber.isNotEmpty() && ValidationUtils.isPhoneNumberValid(fullPhoneNumber)) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_atome_phone_number_invalid)
        }
        return FieldState(fullPhoneNumber, validation)
    }

    private fun validateAddress(
        addressInputModel: AddressInputModel,
        countryOptions: List<AddressListItem>,
        stateOptions: List<AddressListItem>
    ): AddressOutputData {
        return AddressValidationUtils.validateAddressInput(
            addressInputModel,
            AddressFormUIState.FULL_ADDRESS,
            countryOptions,
            stateOptions,
            false
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: AtomeOutputData) {
        val componentState = createComponentState(outputData)
        componentStateChanged(componentState)
    }

    private fun createComponentState(
        outputData: AtomeOutputData = this.outputData
    ): AtomeComponentState {
        val paymentMethod = AtomePaymentMethod(
            // TODO atome
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount.takeUnless { it.isEmpty },
        )

        return AtomeComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true
        )
    }

    private fun componentStateChanged(componentState: AtomeComponentState) {
        _componentStateFlow.tryEmit(componentState)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onSubmit() {
        submitHandler.onSubmit(_componentStateFlow.value)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
