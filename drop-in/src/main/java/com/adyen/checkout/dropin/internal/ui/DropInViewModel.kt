/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.OrderStatusRepository
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.internal.ui.model.DropInActivityEvent
import com.adyen.checkout.dropin.internal.ui.model.DropInDestination
import com.adyen.checkout.dropin.internal.ui.model.DropInOverrideParamsFactory
import com.adyen.checkout.dropin.internal.ui.model.DropInParams
import com.adyen.checkout.dropin.internal.ui.model.GiftCardPaymentConfirmationData
import com.adyen.checkout.dropin.internal.ui.model.OrderModel
import com.adyen.checkout.dropin.internal.util.checkCompileOnly
import com.adyen.checkout.dropin.internal.util.isStoredPaymentSupported
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceStatus
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceUtils
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DropInViewModel(
    private val bundleHandler: DropInSavedStateHandleContainer,
    private val orderStatusRepository: OrderStatusRepository,
    internal val analyticsRepository: AnalyticsRepository,
    private val initialDropInParams: DropInParams,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val eventChannel: Channel<DropInActivityEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    // this should only be used when initializing components, for drop-in related configurations use dropInParams
    val checkoutConfiguration: CheckoutConfiguration = requireNotNull(bundleHandler.checkoutConfiguration)

    val dropInParams: DropInParams
        get() {
            if (overrideAmount == null) return initialDropInParams
            return initialDropInParams.copy(amount = overrideAmount)
        }

    // this is needed for partial payments, the amount has to be updated manually to override the initial amount
    private var overrideAmount: Amount? = null

    val serviceComponentName: ComponentName = requireNotNull(bundleHandler.serviceComponentName)

    private var sessionDetails: SessionDetails?
        get() = bundleHandler.sessionDetails
        private set(value) {
            bundleHandler.sessionDetails = value
        }

    private var isSessionsFlowTakenOver: Boolean
        get() = bundleHandler.isSessionsFlowTakenOver ?: false
        set(value) {
            bundleHandler.isSessionsFlowTakenOver = value
        }

    private var paymentMethodsApiResponse: PaymentMethodsApiResponse
        get() = requireNotNull(bundleHandler.paymentMethodsApiResponse)
        set(value) {
            bundleHandler.paymentMethodsApiResponse = value
        }

    var isWaitingResult: Boolean
        get() = bundleHandler.isWaitingResult ?: false
        set(value) {
            bundleHandler.isWaitingResult = value
        }

    private var cachedGiftCardComponentState: GiftCardComponentState?
        get() = bundleHandler.cachedGiftCardComponentState
        set(value) {
            bundleHandler.cachedGiftCardComponentState = value
        }

    private var cachedPartialPaymentAmount: Amount?
        get() = bundleHandler.cachedPartialPaymentAmount
        set(value) {
            bundleHandler.cachedPartialPaymentAmount = value
        }

    var currentOrder: OrderModel?
        get() = bundleHandler.currentOrder
        private set(value) {
            bundleHandler.currentOrder = value
        }

    private val _addressLookupOptionsFlow = bufferedChannel<List<LookupAddress>>()
    val addressLookupOptionsFlow: Flow<List<LookupAddress>> = _addressLookupOptionsFlow.receiveAsFlow()

    private val _addressLookupCompleteFlow = bufferedChannel<AddressLookupResult>()
    val addressLookupCompleteFlow: Flow<AddressLookupResult> = _addressLookupCompleteFlow.receiveAsFlow()

    fun getPaymentMethods(): List<PaymentMethod> {
        return paymentMethodsApiResponse.paymentMethods.orEmpty()
    }

    fun getStoredPaymentMethods(): List<StoredPaymentMethod> {
        return paymentMethodsApiResponse.storedPaymentMethods.orEmpty()
    }

    fun shouldShowPreselectedStored(): Boolean {
        return getStoredPaymentMethods().any { it.isStoredPaymentSupported() } &&
            dropInParams.showPreselectedStoredPaymentMethod
    }

    fun getPreselectedStoredPaymentMethod(): StoredPaymentMethod {
        return getStoredPaymentMethods().firstOrNull {
            it.isStoredPaymentSupported()
        } ?: StoredPaymentMethod()
    }

    fun getStoredPaymentMethod(id: String): StoredPaymentMethod {
        return getStoredPaymentMethods().firstOrNull { it.id == id } ?: StoredPaymentMethod()
    }

    fun shouldSkipToSinglePaymentMethod(): Boolean {
        if (!dropInParams.skipListWhenSinglePaymentMethod) return false

        val noStored = getStoredPaymentMethods().isEmpty()
        val singlePm = getPaymentMethods().size == 1

        val firstPaymentMethod = getPaymentMethods().firstOrNull()

        val paymentMethodHasComponent = firstPaymentMethod?.let {
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(it.type) &&
                // google pay is supported, is not action only but does not have a UI component inside our code
                !checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(it) } &&
                !PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(it.type)
        } ?: false

        return noStored && singlePm && paymentMethodHasComponent
    }

    /**
     * @return A class needed to initialize components inside drop-in.
     */
    fun getDropInOverrideParams(): DropInOverrideParams {
        val amount = dropInParams.amount
        return DropInOverrideParamsFactory.create(
            amount = amount,
            // when creating a component, the sessions amount has priority over the drop-in amount
            // but after a partial payment is successful the sessions amount is not updated to the remaining amount
            // this is due to the backend not returning the updated amount value in the sessions setup call
            // therefore we modify the amount ourselves here before passing it to the components
            sessionDetails = sessionDetails?.copy(amount = amount),
        )
    }

    fun onCreated() {
        navigateToInitialDestination()
        setupAnalytics()
    }

    fun onDropInServiceConnected() {
        val sessionModel = sessionDetails?.mapToModel()
        if (sessionModel == null) {
            adyenLog(AdyenLogLevel.DEBUG) { "Session is null" }
            return
        }

        val event = DropInActivityEvent.SessionServiceConnected(
            sessionModel = sessionModel,
            clientKey = dropInParams.clientKey,
            environment = dropInParams.environment,
            isFlowTakenOver = isSessionsFlowTakenOver,
        )
        sendEvent(event)
    }

    fun onSessionDataChanged(sessionData: String) {
        sessionDetails = sessionDetails?.copy(sessionData = sessionData)
    }

    fun onSessionTakenOverUpdated(isFlowTakenOver: Boolean) {
        isSessionsFlowTakenOver = isFlowTakenOver
    }

    private fun navigateToInitialDestination() {
        val destination = when {
            shouldSkipToSinglePaymentMethod() -> {
                val firstPaymentMethod = getPaymentMethods().firstOrNull()
                if (firstPaymentMethod != null) {
                    DropInDestination.PaymentComponent(firstPaymentMethod)
                } else {
                    throw CheckoutException("First payment method is null")
                }
            }

            shouldShowPreselectedStored() -> DropInDestination.PreselectedStored
            else -> DropInDestination.PaymentMethods
        }
        sendEvent(DropInActivityEvent.NavigateTo(destination))
    }

    private fun setupAnalytics() {
        adyenLog(AdyenLogLevel.VERBOSE) { "setupAnalytics" }
        viewModelScope.launch {
            analyticsRepository.setupAnalytics()
        }
    }

    /**
     * @return the payment method details required to request the balance, or null if invalid
     */
    fun onBalanceCallRequested(
        giftCardComponentState: GiftCardComponentState
    ): PaymentComponentData<GiftCardPaymentMethod>? {
        val paymentMethod = giftCardComponentState.data.paymentMethod
        if (paymentMethod == null) {
            adyenLog(AdyenLogLevel.ERROR) { "onBalanceCallRequested - paymentMethod is null" }
            return null
        }
        cachedGiftCardComponentState = giftCardComponentState
        return giftCardComponentState.data
    }

    fun handleBalanceResult(balanceResult: BalanceResult): GiftCardBalanceResult {
        adyenLog(AdyenLogLevel.DEBUG) {
            "handleBalanceResult - balance: ${balanceResult.balance} - " +
                "transactionLimit: ${balanceResult.transactionLimit}"
        }

        val giftCardBalanceResult = GiftCardBalanceUtils.checkBalance(
            balance = balanceResult.balance,
            transactionLimit = balanceResult.transactionLimit,
            amountToBePaid = dropInParams.amount,
        )
        val cachedGiftCardComponentState =
            cachedGiftCardComponentState ?: throw CheckoutException("Failed to retrieved cached gift card object")
        return when (giftCardBalanceResult) {
            is GiftCardBalanceStatus.ZeroBalance -> {
                adyenLog(AdyenLogLevel.INFO) { "handleBalanceResult - Gift Card has zero balance" }
                GiftCardBalanceResult.Error(
                    R.string.checkout_giftcard_error_zero_balance,
                    "Gift Card has zero balance",
                    false,
                )
            }

            is GiftCardBalanceStatus.NonMatchingCurrencies -> {
                adyenLog(AdyenLogLevel.ERROR) { "handleBalanceResult - Gift Card currency mismatch" }
                GiftCardBalanceResult.Error(
                    R.string.checkout_giftcard_error_currency,
                    "Gift Card currency mismatch",
                    false,
                )
            }

            is GiftCardBalanceStatus.ZeroAmountToBePaid -> {
                adyenLog(AdyenLogLevel.ERROR) {
                    "handleBalanceResult - You must set an amount in DropInConfiguration.Builder to enable gift " +
                        "card payments"
                }
                GiftCardBalanceResult.Error(R.string.payment_failed, "Drop-in amount is not set", true)
            }

            is GiftCardBalanceStatus.FullPayment -> {
                cachedPartialPaymentAmount = giftCardBalanceResult.amountPaid
                GiftCardBalanceResult.FullPayment(
                    createGiftCardPaymentConfirmationData(giftCardBalanceResult, cachedGiftCardComponentState),
                )
            }

            is GiftCardBalanceStatus.PartialPayment -> {
                cachedPartialPaymentAmount = giftCardBalanceResult.amountPaid
                if (currentOrder == null) {
                    GiftCardBalanceResult.RequestOrderCreation
                } else {
                    GiftCardBalanceResult.RequestPartialPayment
                }
            }
        }
    }

    private fun createGiftCardPaymentConfirmationData(
        giftCardBalanceStatus: GiftCardBalanceStatus.FullPayment,
        giftCardComponentState: GiftCardComponentState
    ): GiftCardPaymentConfirmationData {
        return GiftCardPaymentConfirmationData(
            amountPaid = giftCardBalanceStatus.amountPaid,
            remainingBalance = giftCardBalanceStatus.remainingBalance,
            shopperLocale = dropInParams.shopperLocale,
            brand = giftCardComponentState.data.paymentMethod?.brand.orEmpty(),
            lastFourDigits = giftCardComponentState.lastFourDigits.orEmpty(),
        )
    }

    fun handleOrderCreated(orderResponse: OrderResponse) {
        viewModelScope.launch(coroutineDispatcher) {
            handleOrderResponse(orderResponse)
            if (currentOrder != null) {
                partialPaymentRequested()
            }
        }
    }

    private suspend fun handleOrderResponse(orderResponse: OrderResponse?) {
        val orderModel = getOrderDetails(orderResponse)
        if (orderModel == null) {
            currentOrder = null
            overrideAmount = null
            adyenLog(AdyenLogLevel.DEBUG) { "handleOrderResponse - Amount reverted: ${dropInParams.amount}" }
            adyenLog(AdyenLogLevel.DEBUG) { "handleOrderResponse - Order cancelled" }
        } else {
            currentOrder = orderModel
            overrideAmount = orderModel.remainingAmount
            adyenLog(AdyenLogLevel.DEBUG) { "handleOrderResponse - New amount set: ${dropInParams.amount}" }
            adyenLog(AdyenLogLevel.DEBUG) { "handleOrderResponse - Order cached" }
        }
    }

    fun updatePaymentComponentStateForPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        // include amount value if merchant passed it to the DropIn
        val existingAmount = paymentComponentState.data.amount
        when {
            existingAmount != null -> {
                adyenLog(AdyenLogLevel.DEBUG) { "Payment amount already set: $existingAmount" }
            }

            dropInParams.amount != null -> {
                paymentComponentState.data.amount = dropInParams.amount
                adyenLog(AdyenLogLevel.DEBUG) { "Payment amount set: ${dropInParams.amount}" }
            }

            else -> {
                adyenLog(AdyenLogLevel.DEBUG) { "Payment amount not set" }
            }
        }
        currentOrder?.let {
            paymentComponentState.data.order = createOrder(it)
            adyenLog(AdyenLogLevel.DEBUG) { "Order appended to payment" }
        }
    }

    private fun createOrder(order: OrderModel): OrderRequest {
        return OrderRequest(
            pspReference = order.pspReference,
            orderData = order.orderData,
        )
    }

    fun handlePaymentMethodsUpdate(paymentMethodsApiResponse: PaymentMethodsApiResponse, order: OrderResponse?) {
        viewModelScope.launch(coroutineDispatcher) {
            handleOrderResponse(order)
            this@DropInViewModel.paymentMethodsApiResponse = paymentMethodsApiResponse
            sendEvent(DropInActivityEvent.ShowPaymentMethods)
        }
    }

    fun onToPaymentMethodsList(paymentMethodsApiResponse: PaymentMethodsApiResponse?) {
        paymentMethodsApiResponse?.let {
            this.paymentMethodsApiResponse = it
        }
        sendEvent(DropInActivityEvent.ShowPaymentMethods)
    }

    fun removeStoredPaymentMethodWithId(id: String) {
        val positionToRemove = getStoredPaymentMethods().indexOfFirst { it.id == id }
        val updatedStoredPaymentMethods = getStoredPaymentMethods().toMutableList()
        if (positionToRemove != -1) {
            updatedStoredPaymentMethods.removeAt(positionToRemove)
            paymentMethodsApiResponse.storedPaymentMethods = updatedStoredPaymentMethods
        }
    }

    private suspend fun getOrderDetails(orderResponse: OrderResponse?): OrderModel? {
        if (orderResponse == null) return null

        return orderStatusRepository.getOrderStatus(dropInParams.clientKey, orderResponse.orderData)
            .fold(
                onSuccess = { statusResponse ->
                    OrderModel(
                        orderData = orderResponse.orderData,
                        pspReference = orderResponse.pspReference,
                        remainingAmount = statusResponse.remainingAmount,
                        paymentMethods = statusResponse.paymentMethods,
                    )
                },
                onFailure = { e ->
                    adyenLog(AdyenLogLevel.ERROR, e) { "Unable to fetch order details" }
                    null
                },
            )
    }

    fun partialPaymentRequested() {
        val paymentComponentState = cachedGiftCardComponentState
            ?: throw CheckoutException("Lost reference to cached GiftCardComponentState")
        val partialPaymentAmount = cachedPartialPaymentAmount
            ?: throw CheckoutException("Lost reference to cached partial payment amount")
        paymentComponentState.data.amount = partialPaymentAmount
        adyenLog(AdyenLogLevel.DEBUG) { "Partial payment amount set: $partialPaymentAmount" }
        cachedGiftCardComponentState = null
        cachedPartialPaymentAmount = null
        sendEvent(DropInActivityEvent.MakePartialPayment(paymentComponentState))
    }

    fun orderCancellationRequested() {
        val order = currentOrder
            ?: throw CheckoutException("No order in progress")
        sendCancelOrderEvent(order, false)
    }

    fun onAddressLookupOptions(options: List<LookupAddress>) {
        adyenLog(AdyenLogLevel.DEBUG) { "onAddressLookupOptions $options" }
        viewModelScope.launch { _addressLookupOptionsFlow.send(options) }
    }

    fun onAddressLookupComplete(lookupAddress: LookupAddress) {
        adyenLog(AdyenLogLevel.DEBUG) { "onAddressLookupComplete $lookupAddress" }
        viewModelScope.launch { _addressLookupCompleteFlow.send(AddressLookupResult.Completed(lookupAddress)) }
    }

    fun cancelDropIn() {
        currentOrder?.let { sendCancelOrderEvent(it, true) }
        sendEvent(DropInActivityEvent.CancelDropIn)
    }

    private fun sendCancelOrderEvent(order: OrderModel, isDropInCancelledByUser: Boolean) {
        val orderRequest = OrderRequest(
            pspReference = order.pspReference,
            orderData = order.orderData,
        )
        sendEvent(DropInActivityEvent.CancelOrder(orderRequest, isDropInCancelledByUser))
    }

    private fun sendEvent(event: DropInActivityEvent) {
        viewModelScope.launch {
            adyenLog(AdyenLogLevel.DEBUG) { "sendEvent - ${event::class.simpleName}" }
            eventChannel.send(event)
        }
    }
}
