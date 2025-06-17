/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import app.cash.paykit.core.CashAppPay
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayListener
import app.cash.paykit.core.CashAppPayState
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.response.GrantType
import app.cash.paykit.core.models.sdk.CashAppPayCurrency
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.CashAppPayEnvironment
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayAuthorizationData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParams
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayInputData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOnFileData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOneTimeData
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayOutputData
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.CashAppPayPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DefaultCashAppPayDelegate
@Suppress("LongParameterList")
constructor(
    private val submitHandler: SubmitHandler<CashAppPayComponentState>,
    private val analyticsManager: AnalyticsManager,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: CashAppPayComponentParams,
    private val cashAppPayFactory: CashAppPayFactory,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) : CashAppPayDelegate, ButtonDelegate, CashAppPayListener {

    private val inputData = CashAppPayInputData()

    private var outputData = createOutputData()

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<CashAppPayComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(CashAppPayComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val submitFlow: Flow<CashAppPayComponentState> = getTrackedSubmitFlow()

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private lateinit var cashAppPay: CashAppPay

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        submitHandler.initialize(coroutineScope, componentStateFlow)

        cashAppPay = initCashAppPay()

        initializeAnalytics(coroutineScope)

        if (!isConfirmationRequired()) {
            initiatePayment()
        }
    }

    private fun initCashAppPay(): CashAppPay {
        return if (componentParams.cashAppPayEnvironment == CashAppPayEnvironment.SANDBOX) {
            cashAppPayFactory.createSandbox(componentParams.requireClientId())
        } else {
            cashAppPayFactory.create(componentParams.requireClientId())
        }.apply {
            registerForStateUpdates(this@DefaultCashAppPayDelegate)
        }
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<CashAppPayComponentState>) -> Unit
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

    override fun updateInputData(update: CashAppPayInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        outputData = createOutputData()
        updateComponentState(outputData)
    }

    private fun createOutputData(): CashAppPayOutputData {
        return CashAppPayOutputData(
            isStorePaymentSelected = inputData.isStorePaymentSelected,
            authorizationData = inputData.authorizationData,
        )
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: CashAppPayOutputData) {
        adyenLog(AdyenLogLevel.VERBOSE) { "updateComponentState" }
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    private fun createComponentState(
        outputData: CashAppPayOutputData = this.outputData
    ): CashAppPayComponentState {
        val oneTimeData = outputData.authorizationData?.oneTimeData
        val onFileData = outputData.authorizationData?.onFileData

        val cashAppPayPaymentMethod = CashAppPayPaymentMethod(
            type = paymentMethod.type,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            grantId = oneTimeData?.grantId,
            customerId = onFileData?.customerId ?: oneTimeData?.customerId,
            onFileGrantId = onFileData?.grantId,
            cashtag = onFileData?.cashTag,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = cashAppPayPaymentMethod,
            order = order,
            amount = componentParams.amount,
            storePaymentMethod = onFileData != null,
        )

        return CashAppPayComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
        )
    }

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        if (isConfirmationRequired()) {
            initiatePayment()
        }
    }

    private fun initiatePayment() {
        val actions = listOfNotNull(
            getOneTimeAction(),
            getOnFileAction(outputData),
        )

        if (actions.isEmpty()) {
            exceptionChannel.trySend(
                ComponentException(
                    "Cannot launch Cash App Pay, you need to either pass an amount with supported " +
                        "currency or store the shopper account.",
                ),
            )
            return
        }

        _viewFlow.tryEmit(PaymentInProgressViewType)

        coroutineScope.launch(coroutineDispatcher) {
            cashAppPay.createCustomerRequest(actions, componentParams.returnUrl)
        }
    }

    @Suppress("ReturnCount")
    private fun getOneTimeAction(): CashAppPayPaymentAction.OneTimeAction? {
        val amount = componentParams.amount

        if (amount?.value == null || amount.value == 0L) return null

        val cashAppPayCurrency = when (amount.currency) {
            CheckoutCurrency.USD.name -> CashAppPayCurrency.USD
            else -> {
                exceptionChannel.trySend(ComponentException("Unsupported currency: ${amount.currency}"))
                return null
            }
        }

        return CashAppPayPaymentAction.OneTimeAction(
            amount = amount.value.toInt(),
            currency = cashAppPayCurrency,
            scopeId = componentParams.scopeId,
        )
    }

    private fun getOnFileAction(
        outputData: CashAppPayOutputData,
    ): CashAppPayPaymentAction.OnFileAction? {
        val shouldStorePaymentMethod = when {
            // Shopper is presented with store switch and selected it
            componentParams.showStorePaymentField && outputData.isStorePaymentSelected -> true
            // Shopper is not presented with store switch and configuration indicates storing the payment method
            !componentParams.showStorePaymentField && componentParams.storePaymentMethod -> true
            else -> false
        }

        // We don't create an OnFileAction when storing is not required
        if (!shouldStorePaymentMethod) return null

        return CashAppPayPaymentAction.OnFileAction(
            scopeId = componentParams.scopeId,
        )
    }

    override fun cashAppPayStateDidChange(newState: CashAppPayState) {
        adyenLog(AdyenLogLevel.DEBUG) { "CashAppPayState state changed: ${newState::class.simpleName}" }
        when (newState) {
            is CashAppPayState.ReadyToAuthorize -> {
                cashAppPay.authorizeCustomerRequest()
            }

            is CashAppPayState.Approved -> {
                adyenLog(AdyenLogLevel.INFO) { "Cash App Pay authorization request approved" }
                updateInputData {
                    authorizationData = createAuthorizationData(newState.responseData)
                }

                submitHandler.onSubmit(_componentStateFlow.value)
            }

            CashAppPayState.Declined -> {
                adyenLog(AdyenLogLevel.INFO) { "Cash App Pay authorization request declined" }
                exceptionChannel.trySend(ComponentException("Cash App Pay authorization request declined"))
            }

            is CashAppPayState.CashAppPayExceptionState -> {
                trackThirdPartyErrorEvent()
                exceptionChannel.trySend(
                    ComponentException("Cash App Pay has encountered an error", newState.exception),
                )
            }

            else -> Unit
        }
    }

    private fun createAuthorizationData(customerResponseData: CustomerResponseData): CashAppPayAuthorizationData {
        val grants = customerResponseData.grants.orEmpty()
        val oneTimeData =
            grants.find { it.type == GrantType.ONE_TIME }?.let { CashAppPayOneTimeData(it.id, it.customerId) }
        val onFileData = grants.find { it.type == GrantType.EXTENDED }?.let {
            CashAppPayOnFileData(
                grantId = it.id,
                cashTag = customerResponseData.customerProfile?.cashTag?.toString(),
                customerId = customerResponseData.customerProfile?.id,
            )
        }

        return CashAppPayAuthorizationData(
            oneTimeData = oneTimeData,
            onFileData = onFileData,
        )
    }

    private fun trackThirdPartyErrorEvent() {
        val event = GenericEvents.error(
            component = getPaymentMethodType(),
            event = ErrorEvent.THIRD_PARTY,
        )
        analyticsManager.trackEvent(event)
    }

    override fun isConfirmationRequired(): Boolean =
        _viewFlow.value is ButtonComponentViewType &&
            componentParams.showStorePaymentField

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    internal fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        _coroutineScope = null
        removeObserver()
        cashAppPay.unregisterFromStateUpdates()
        analyticsManager.clear(this)
    }
}
