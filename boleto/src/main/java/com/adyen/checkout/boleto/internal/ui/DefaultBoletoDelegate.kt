/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.boleto.BoletoComponentState
import com.adyen.checkout.boleto.internal.ui.model.BoletoComponentParams
import com.adyen.checkout.boleto.internal.ui.model.BoletoInputData
import com.adyen.checkout.boleto.internal.ui.model.BoletoOutputData
import com.adyen.checkout.boleto.internal.util.BoletoValidationUtils
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.ShopperName
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.data.api.AddressRepository
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import com.adyen.checkout.ui.core.internal.util.SocialSecurityNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultBoletoDelegate(
    private val submitHandler: SubmitHandler<BoletoComponentState>,
    private val analyticsManager: AnalyticsManager,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: BoletoComponentParams,
    private val addressRepository: AddressRepository,
) : BoletoDelegate {
    private val inputData = BoletoInputData()

    override val outputData: BoletoOutputData get() = _outputDataFlow.value

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BoletoOutputData> = _outputDataFlow

    override val addressOutputData: AddressOutputData
        get() = outputData.addressState

    override val addressOutputDataFlow: Flow<AddressOutputData> by lazy {
        outputDataFlow.map {
            it.addressState
        }.stateIn(coroutineScope, SharingStarted.Lazily, outputData.addressState)
    }

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<BoletoComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(BoletoComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<BoletoComponentState> = submitHandler.submitFlow
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        submitHandler.initialize(coroutineScope, componentStateFlow)

        initializeAnalytics(coroutineScope)

        if (componentParams.addressParams is AddressParams.FullAddress) {
            subscribeToStatesList()
            subscribeToCountryList()
            requestCountryList()
        }
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    private fun subscribeToStatesList() {
        addressRepository.statesFlow
            .distinctUntilChanged()
            .onEach { states ->
                adyenLog(AdyenLogLevel.DEBUG) { "New states emitted - states: ${states.size}" }
                updateOutputData(stateOptions = AddressFormUtils.initializeStateOptions(states))
            }
            .launchIn(coroutineScope)
    }

    private fun subscribeToCountryList() {
        addressRepository.countriesFlow
            .distinctUntilChanged()
            .onEach { countries ->
                val countryOptions = AddressFormUtils.initializeCountryOptions(
                    shopperLocale = componentParams.shopperLocale,
                    addressParams = componentParams.addressParams,
                    countryList = countries,
                )
                countryOptions.firstOrNull { it.selected }?.let {
                    inputData.address.country = it.code
                    requestStateList(it.code)
                }
                updateOutputData(countryOptions = countryOptions)
            }
            .launchIn(coroutineScope)
    }

    private fun requestCountryList() {
        addressRepository.getCountryList(
            shopperLocale = componentParams.shopperLocale,
            coroutineScope = coroutineScope,
        )
    }

    private fun requestStateList(countryCode: String?) {
        addressRepository.getStateList(
            shopperLocale = componentParams.shopperLocale,
            countryCode = countryCode,
            coroutineScope = coroutineScope,
        )
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        updateInputData {
            this.address.update()
        }
    }

    override fun updateInputData(update: BoletoInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData(
            countryOptions = outputData.addressState.countryOptions,
            stateOptions = outputData.addressState.stateOptions,
        )
        _outputDataFlow.tryEmit(outputData)
        updateComponentState(outputData)
        requestStateList(inputData.address.country)
    }

    private fun updateOutputData(
        countryOptions: List<AddressListItem> = outputData.addressState.countryOptions,
        stateOptions: List<AddressListItem> = outputData.addressState.stateOptions,
    ) {
        val newOutputData = createOutputData(countryOptions, stateOptions)
        _outputDataFlow.tryEmit(newOutputData)
        updateComponentState(newOutputData)
    }

    private fun createOutputData(
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
    ): BoletoOutputData {
        val updatedCountryOptions = AddressFormUtils.markAddressListItemSelected(
            countryOptions,
            inputData.address.country,
        )
        val updatedStateOptions = AddressFormUtils.markAddressListItemSelected(
            stateOptions,
            inputData.address.stateOrProvince,
        )

        val addressFormUIState = AddressFormUIState.fromAddressParams(componentParams.addressParams)

        return BoletoOutputData(
            firstNameState = BoletoValidationUtils.validateFirstName(inputData.firstName),
            lastNameState = BoletoValidationUtils.validateLastName(inputData.lastName),
            socialSecurityNumberState = SocialSecurityNumberUtils.validateSocialSecurityNumber(
                inputData.socialSecurityNumber,
            ),
            addressState = AddressValidationUtils.validateAddressInput(
                inputData.address,
                addressFormUIState,
                updatedCountryOptions,
                updatedStateOptions,
                false,
            ),
            addressUIState = addressFormUIState,
            isEmailVisible = componentParams.isEmailVisible,
            isSendEmailSelected = inputData.isSendEmailSelected,
            shopperEmailState = BoletoValidationUtils.validateShopperEmail(
                inputData.isSendEmailSelected,
                inputData.shopperEmail,
            ),
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: BoletoOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: BoletoOutputData = this.outputData
    ): BoletoComponentState {
        val paymentComponentData = PaymentComponentData(
            paymentMethod = GenericPaymentMethod(
                type = paymentMethod.type,
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                subtype = null,
            ),
            order = order,
            amount = componentParams.amount,
            socialSecurityNumber = outputData.socialSecurityNumberState.value,
            shopperName = ShopperName(
                firstName = outputData.firstNameState.value,
                lastName = outputData.lastNameState.value,
            ),
        )
        if (outputData.isSendEmailSelected) {
            paymentComponentData.shopperEmail = outputData.shopperEmailState.value
        }
        if (AddressFormUtils.isAddressRequired(outputData.addressUIState)) {
            paymentComponentData.billingAddress = AddressFormUtils.makeAddressData(
                addressOutputData = outputData.addressState,
                addressFormUIState = outputData.addressUIState,
            )
        }
        val countriesList: List<AddressListItem> = outputData.addressState.countryOptions
        val statesList: List<AddressListItem> = outputData.addressState.stateOptions

        return BoletoComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = countriesList.isNotEmpty() && statesList.isNotEmpty(),
        )
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<BoletoComponentState>) -> Unit
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

    override fun onSubmit() {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        submitHandler.onSubmit(_componentStateFlow.value)
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun shouldEnableSubmitButton(): Boolean = true

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
