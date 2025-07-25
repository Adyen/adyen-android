/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.ShopperName
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.paymentmethod.PayToPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.payto.R
import com.adyen.checkout.payto.internal.ui.model.PayIdType
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel
import com.adyen.checkout.payto.internal.ui.model.PayToInputData
import com.adyen.checkout.payto.internal.ui.model.PayToMode
import com.adyen.checkout.payto.internal.ui.model.PayToOutputData
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.old.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.old.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class DefaultPayToDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<PayToComponentState>,
) : PayToDelegate, ButtonDelegate, UIStateDelegate {

    private val inputData: PayToInputData = PayToInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayToOutputData> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PayToComponentState> = _componentStateFlow

    override val outputData: PayToOutputData get() = _outputDataFlow.value

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(PayToComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<PayToComponentState> = getTrackedSubmitFlow()

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

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
        callback: (PaymentComponentEvent<PayToComponentState>) -> Unit
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

    override fun getPaymentMethodType() = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun getPayIdTypes(): List<PayIdTypeModel> = SUPPORTED_PAY_ID_TYPES

    override fun updateInputData(update: PayToInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        adyenLog(AdyenLogLevel.VERBOSE) { "onInputDataChanged" }
        val outputData = createOutputData()
        outputDataChanged(outputData)
        updateComponentState(outputData)
    }

    private fun createOutputData() = PayToOutputData(
        mode = inputData.mode,
        payIdTypeModel = inputData.payIdTypeModel,
        mobilePhoneNumber = inputData.phoneNumber.trimStart('0'),
        emailAddress = inputData.emailAddress,
        abnNumber = inputData.abnNumber,
        organizationId = inputData.organizationId,
        bsbStateBranch = inputData.bsbStateBranch,
        bsbAccountNumber = inputData.bsbAccountNumber,
        firstName = inputData.firstName,
        lastName = inputData.lastName,
    )

    private fun outputDataChanged(outputData: PayToOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: PayToOutputData) {
        val componentState = createComponentState(outputData)
        componentStateChanged(componentState)
    }

    private fun createComponentState(
        outputData: PayToOutputData = this.outputData
    ): PayToComponentState {
        val paymentMethod = PayToPaymentMethod(
            type = getPaymentMethodType(),
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            shopperAccountIdentifier = getShopperAccountIdentifier(outputData),
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
            shopperName = ShopperName(
                firstName = outputData.firstNameFieldState.value,
                lastName = outputData.lastNameFieldState.value,
            ),
        )

        return PayToComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
        )
    }

    private fun getShopperAccountIdentifier(outputData: PayToOutputData) = when (outputData.mode) {
        PayToMode.PAY_ID -> getShopperAccountIdentifierForPayId(outputData)
        PayToMode.BSB -> getShopperAccountIdentifierForBsb(outputData)
    }

    private fun getShopperAccountIdentifierForPayId(outputData: PayToOutputData) =
        when (outputData.payIdTypeModel?.type) {
            PayIdType.PHONE -> "$PHONE_NUMBER_PREFIX-${outputData.phoneNumberFieldState.value}"
            PayIdType.EMAIL -> outputData.emailAddressFieldState.value
            PayIdType.ABN -> outputData.abnNumberFieldState.value
            PayIdType.ORGANIZATION_ID -> outputData.organizationIdFieldState.value
            null -> ""
        }

    private fun getShopperAccountIdentifierForBsb(outputData: PayToOutputData) =
        "${outputData.bsbStateBranch}-${outputData.bsbAccountNumber}"

    private fun componentStateChanged(componentState: PayToComponentState) {
        _componentStateFlow.tryEmit(componentState)
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
        private val SUPPORTED_PAY_ID_TYPES = listOf(
            PayIdTypeModel(PayIdType.PHONE, R.string.checkout_payto_payid_type_phone_number),
            PayIdTypeModel(PayIdType.EMAIL, R.string.checkout_payto_payid_type_email_address),
            PayIdTypeModel(PayIdType.ABN, R.string.checkout_payto_payid_type_abn_number),
            PayIdTypeModel(PayIdType.ORGANIZATION_ID, R.string.checkout_payto_payid_type_organization_id),
        )

        private const val PHONE_NUMBER_PREFIX = "+61"
    }
}
