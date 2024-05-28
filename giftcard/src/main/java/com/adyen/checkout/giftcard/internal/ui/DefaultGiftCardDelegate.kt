/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/7/2022.
 */

package com.adyen.checkout.giftcard.internal.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.giftcard.GiftCardAction
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.GiftCardException
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardComponentParams
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardInputData
import com.adyen.checkout.giftcard.internal.ui.model.GiftCardOutputData
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceStatus
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardPinUtils
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
internal class DefaultGiftCardDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    private val analyticsManager: AnalyticsManager,
    private val publicKeyRepository: PublicKeyRepository,
    override val componentParams: GiftCardComponentParams,
    private val cardEncryptor: BaseCardEncryptor,
    private val submitHandler: SubmitHandler<GiftCardComponentState>,
) : GiftCardDelegate {

    private val inputData: GiftCardInputData = GiftCardInputData()

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<GiftCardOutputData> = _outputDataFlow

    override val outputData get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<GiftCardComponentState> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(GiftCardComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val submitFlow: Flow<GiftCardComponentState> = submitHandler.submitFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private var cachedAmount: Amount? = null

    private var publicKey: String? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        submitHandler.initialize(coroutineScope, componentStateFlow)
        initializeAnalytics(coroutineScope)
        fetchPublicKey(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
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

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<GiftCardComponentState>) -> Unit
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

    override fun updateInputData(update: GiftCardInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()

        _outputDataFlow.tryEmit(outputData)

        updateComponentState(outputData)
    }

    private fun createOutputData() = GiftCardOutputData(
        numberFieldState = GiftCardNumberUtils.validateInputField(inputData.cardNumber),
        pinFieldState = getPinFieldState(inputData.pin),
    )

    private fun getPinFieldState(pin: String): FieldState<String> {
        return if (isPinRequired()) {
            GiftCardPinUtils.validateInputField(pin)
        } else {
            FieldState(pin, Validation.Valid)
        }
    }

    @VisibleForTesting
    internal fun updateComponentState(outputData: GiftCardOutputData) {
        val componentState = createComponentState(outputData)
        _componentStateFlow.tryEmit(componentState)
    }

    @Suppress("ReturnCount")
    private fun createComponentState(
        outputData: GiftCardOutputData = this.outputData
    ): GiftCardComponentState {
        val publicKey = publicKey ?: return GiftCardComponentState(
            data = PaymentComponentData(null, null, null),
            isInputValid = outputData.isValid,
            isReady = false,
            lastFourDigits = null,
            giftCardAction = GiftCardAction.Idle,
        )

        if (!outputData.isValid) {
            return GiftCardComponentState(
                data = PaymentComponentData(null, null, null),
                isInputValid = false,
                isReady = true,
                lastFourDigits = null,
                giftCardAction = GiftCardAction.Idle,
            )
        }

        val encryptedCard = encryptCard(outputData, publicKey) ?: return GiftCardComponentState(
            data = PaymentComponentData(null, null, null),
            isInputValid = false,
            isReady = true,
            lastFourDigits = null,
            giftCardAction = GiftCardAction.Idle,
        )

        val giftCardPaymentMethod = GiftCardPaymentMethod(
            type = GiftCardPaymentMethod.PAYMENT_METHOD_TYPE,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            encryptedCardNumber = encryptedCard.encryptedCardNumber,
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
            brand = paymentMethod.brand,
        )

        val lastDigits = outputData.numberFieldState.value.takeLast(LAST_DIGITS_LENGTH)

        val paymentComponentData = PaymentComponentData(
            paymentMethod = giftCardPaymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return GiftCardComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
            lastFourDigits = lastDigits,
            giftCardAction = GiftCardAction.CheckBalance,
        )
    }

    override fun onSubmit() {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)

        val state = _componentStateFlow.value
        submitHandler.onSubmit(state)
    }

    private fun encryptCard(
        outputData: GiftCardOutputData,
        publicKey: String,
    ): EncryptedCard? = try {
        val unencryptedCard = UnencryptedCard.Builder().run {
            setNumber(outputData.numberFieldState.value)
            if (componentParams.isPinRequired) {
                setCvc(outputData.pinFieldState.value)
            }
            build()
        }

        cardEncryptor.encryptFields(unencryptedCard, publicKey)
    } catch (e: EncryptionException) {
        exceptionChannel.trySend(e)
        null
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun isConfirmationRequired(): Boolean = _viewFlow.value is ButtonComponentViewType

    override fun shouldShowSubmitButton(): Boolean = isConfirmationRequired() && componentParams.isSubmitButtonVisible

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        submitHandler.setInteractionBlocked(isInteractionBlocked)
    }

    override fun resolveBalanceResult(balanceResult: BalanceResult) {
        val balanceStatus = GiftCardBalanceUtils.checkBalance(
            balance = balanceResult.balance,
            transactionLimit = balanceResult.transactionLimit,
            amountToBePaid = componentParams.amount,
        )

        resolveBalanceStatus(balanceStatus)
    }

    @VisibleForTesting
    internal fun resolveBalanceStatus(balanceStatus: GiftCardBalanceStatus) {
        val currentState = _componentStateFlow.value
        when (balanceStatus) {
            is GiftCardBalanceStatus.FullPayment -> {
                val updatedState = currentState.copy(
                    giftCardAction = GiftCardAction.SendPayment,
                )
                _componentStateFlow.tryEmit(updatedState)
                submitHandler.onSubmit(updatedState)
            }

            is GiftCardBalanceStatus.NonMatchingCurrencies -> {
                exceptionChannel.trySend(
                    GiftCardException("Currency of the gift card does not match the currency of transaction."),
                )
            }

            is GiftCardBalanceStatus.PartialPayment -> {
                val updatedState = if (order == null) {
                    currentState.copy(giftCardAction = GiftCardAction.CreateOrder)
                } else {
                    currentState.copy(
                        giftCardAction = GiftCardAction.SendPayment,
                        data = currentState.data.copy(
                            amount = balanceStatus.amountPaid,
                        ),
                    )
                }
                cachedAmount = balanceStatus.amountPaid
                _componentStateFlow.tryEmit(updatedState)
                submitHandler.onSubmit(updatedState)
            }

            is GiftCardBalanceStatus.ZeroAmountToBePaid -> {
                exceptionChannel.trySend(
                    GiftCardException("Amount of the transaction is zero."),
                )
            }

            is GiftCardBalanceStatus.ZeroBalance -> {
                exceptionChannel.trySend(
                    GiftCardException("Gift card has no balance."),
                )
            }
        }
    }

    override fun resolveOrderResponse(orderResponse: OrderResponse) {
        val currentState = _componentStateFlow.value
        val updatedState = currentState.copy(
            giftCardAction = GiftCardAction.SendPayment,
            data = currentState.data.copy(
                order = OrderRequest(
                    orderData = orderResponse.orderData,
                    pspReference = orderResponse.pspReference,
                ),
                amount = cachedAmount,
            ),
        )
        cachedAmount = null
        _componentStateFlow.tryEmit(updatedState)
        submitHandler.onSubmit(updatedState)
    }

    override fun isPinRequired(): Boolean = componentParams.isPinRequired

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }

    companion object {
        private const val LAST_DIGITS_LENGTH = 4
    }
}
