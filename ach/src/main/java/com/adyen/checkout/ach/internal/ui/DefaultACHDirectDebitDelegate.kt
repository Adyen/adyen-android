/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.ach.ACHDirectDebitComponentState
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitComponentParams
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitInputData
import com.adyen.checkout.ach.internal.ui.model.ACHDirectDebitOutputData
import com.adyen.checkout.ach.internal.util.ACHDirectDebitValidationUtils
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.ACHDirectDebitPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.checkout.ui.core.internal.data.api.AddressRepository
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("LongParameterList", "TooManyFunctions")
internal class DefaultACHDirectDebitDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val analyticsManager: AnalyticsManager,
    private val publicKeyRepository: PublicKeyRepository,
    private val addressRepository: AddressRepository,
    private val submitHandler: SubmitHandler<ACHDirectDebitComponentState>,
    private val genericEncryptor: BaseGenericEncryptor,
    override val componentParams: ACHDirectDebitComponentParams,
    private val order: Order?
) : ACHDirectDebitDelegate, ButtonDelegate, UIStateDelegate {

    private val inputData: ACHDirectDebitInputData = ACHDirectDebitInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<ACHDirectDebitOutputData> = _outputDataFlow

    override val outputData: ACHDirectDebitOutputData
        get() = _outputDataFlow.value

    override val addressOutputData: AddressOutputData
        get() = outputData.addressState

    override val addressOutputDataFlow: Flow<AddressOutputData> by lazy {
        outputDataFlow.map {
            it.addressState
        }.stateIn(coroutineScope, SharingStarted.Lazily, outputData.addressState)
    }

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<ACHDirectDebitComponentState> = _componentStateFlow

    private var publicKey: String? = null

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(ACHDirectDebitComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<ACHDirectDebitComponentState> = submitHandler.submitFlow
    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow
    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        submitHandler.initialize(coroutineScope, componentStateFlow)

        initializeAnalytics(coroutineScope)
        fetchPublicKey(coroutineScope)

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

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        updateInputData {
            this.address.update()
        }
    }

    override fun updateInputData(update: ACHDirectDebitInputData.() -> Unit) {
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

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    private fun createOutputData(
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
    ): ACHDirectDebitOutputData {
        val updatedCountryOptions = AddressFormUtils.markAddressListItemSelected(
            countryOptions,
            inputData.address.country,
        )
        val updatedStateOptions = AddressFormUtils.markAddressListItemSelected(
            stateOptions,
            inputData.address.stateOrProvince,
        )

        val addressFormUIState = AddressFormUIState.fromAddressParams(componentParams.addressParams)

        return ACHDirectDebitOutputData(
            bankAccountNumber = ACHDirectDebitValidationUtils.validateBankAccountNumber(inputData.bankAccountNumber),
            bankLocationId = ACHDirectDebitValidationUtils.validateBankLocationId(inputData.bankLocationId),
            ownerName = ACHDirectDebitValidationUtils.validateOwnerName(inputData.ownerName),
            addressState = AddressValidationUtils.validateAddressInput(
                inputData.address,
                addressFormUIState,
                updatedCountryOptions,
                updatedStateOptions,
                false,
            ),
            addressUIState = addressFormUIState,
            shouldStorePaymentMethod = inputData.isStorePaymentMethodSwitchChecked,
            showStorePaymentField = showStorePaymentField(),
        )
    }

    private fun fetchPublicKey(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.DEBUG) { "fetchPublicKey" }
        coroutineScope.launch {
            publicKeyRepository.fetchPublicKey(
                environment = componentParams.environment,
                clientKey = componentParams.clientKey,
            ).fold(
                onSuccess = { key ->
                    adyenLog(AdyenLogLevel.DEBUG) { "Public key fetched" }
                    publicKey = key
                    updateComponentState(outputData)
                },
                onFailure = { e ->
                    adyenLog(AdyenLogLevel.ERROR) { "Unable to fetch public key" }
                    exceptionChannel.trySend(ComponentException("Unable to fetch publicKey.", e))
                },
            )
        }
    }

    private fun subscribeToCountryList() {
        addressRepository.countriesFlow
            .distinctUntilChanged()
            .onEach { countries ->
                adyenLog(AdyenLogLevel.DEBUG) { "New countries emitted - countries: ${countries.size}" }
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

    private fun subscribeToStatesList() {
        addressRepository.statesFlow
            .distinctUntilChanged()
            .onEach { states ->
                adyenLog(AdyenLogLevel.DEBUG) { "New states emitted - states: ${states.size}" }
                updateOutputData(stateOptions = AddressFormUtils.initializeStateOptions(states))
            }
            .launchIn(coroutineScope)
    }

    private fun updateOutputData(
        countryOptions: List<AddressListItem> = outputData.addressState.countryOptions,
        stateOptions: List<AddressListItem> = outputData.addressState.stateOptions,
    ) {
        val newOutputData = createOutputData(countryOptions, stateOptions)
        _outputDataFlow.tryEmit(newOutputData)
        updateComponentState(newOutputData)
    }

    private fun requestStateList(countryCode: String?) {
        addressRepository.getStateList(
            shopperLocale = componentParams.shopperLocale,
            countryCode = countryCode,
            coroutineScope = coroutineScope,
        )
    }

    private fun requestCountryList() {
        addressRepository.getCountryList(
            shopperLocale = componentParams.shopperLocale,
            coroutineScope = coroutineScope,
        )
    }

    private fun updateComponentState(outputData: ACHDirectDebitOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    @Suppress("ReturnCount")
    private fun createComponentState(
        outputData: ACHDirectDebitOutputData = this.outputData
    ): ACHDirectDebitComponentState {
        val publicKey = publicKey
        if (!outputData.isValid || publicKey == null) {
            return ACHDirectDebitComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = outputData.isValid,
                isReady = publicKey != null,
            )
        }

        try {
            val encryptedBankAccountNumber = genericEncryptor.encryptField(
                fieldKeyToEncrypt = ENCRYPTION_KEY_FOR_BANK_ACCOUNT_NUMBER,
                fieldValueToEncrypt = outputData.bankAccountNumber.value,
                publicKey = publicKey,
            )
            val encryptedBankLocationId = genericEncryptor.encryptField(
                fieldKeyToEncrypt = ENCRYPTION_KEY_FOR_BANK_LOCATION_ID,
                fieldValueToEncrypt = outputData.bankLocationId.value,
                publicKey = publicKey,
            )

            val achPaymentMethod = ACHDirectDebitPaymentMethod(
                type = ACHDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE,
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                encryptedBankAccountNumber = encryptedBankAccountNumber,
                encryptedBankLocationId = encryptedBankLocationId,
                ownerName = outputData.ownerName.value,
            )
            val paymentComponentData = PaymentComponentData(
                order = order,
                storePaymentMethod = if (showStorePaymentField()) outputData.shouldStorePaymentMethod else null,
                paymentMethod = achPaymentMethod,
                amount = componentParams.amount,
            )

            if (isAddressRequired(outputData.addressUIState)) {
                paymentComponentData.billingAddress = AddressFormUtils.makeAddressData(
                    addressOutputData = outputData.addressState,
                    addressFormUIState = outputData.addressUIState,
                )
            }

            return ACHDirectDebitComponentState(paymentComponentData, isInputValid = true, isReady = true)
        } catch (e: EncryptionException) {
            exceptionChannel.trySend(e)
            return ACHDirectDebitComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = false,
                isReady = true,
            )
        }
    }

    private fun isAddressRequired(addressFormUIState: AddressFormUIState): Boolean {
        return AddressFormUtils.isAddressRequired(addressFormUIState)
    }

    private fun showStorePaymentField(): Boolean {
        return componentParams.isStorePaymentFieldVisible
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

    internal fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun onCleared() {
        removeObserver()
        _coroutineScope = null
        analyticsManager.clear(this)
    }

    override fun onSubmit() {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        val state = _componentStateFlow.value
        submitHandler.onSubmit(
            state = state,
        )
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    companion object {
        private const val ENCRYPTION_KEY_FOR_BANK_ACCOUNT_NUMBER = "bankAccountNumber"
        private const val ENCRYPTION_KEY_FOR_BANK_LOCATION_ID = "bankLocationId"
    }
}
