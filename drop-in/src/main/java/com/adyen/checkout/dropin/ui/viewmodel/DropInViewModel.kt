/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.OrderStatusRepository
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.internal.util.isEmpty
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.giftcard.GiftCardBalanceResult
import com.adyen.checkout.dropin.ui.giftcard.GiftCardPaymentConfirmationData
import com.adyen.checkout.dropin.ui.order.OrderModel
import com.adyen.checkout.dropin.ui.stored.isStoredPaymentSupported
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceStatus
import com.adyen.checkout.giftcard.internal.util.GiftCardBalanceUtils
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DropInViewModel(
    private val bundleHandler: DropInSavedStateHandleContainer,
    private val orderStatusRepository: OrderStatusRepository,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val eventChannel: Channel<DropInActivityEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    val dropInConfiguration: DropInConfiguration = requireNotNull(bundleHandler.dropInConfiguration)

    val serviceComponentName: ComponentName = requireNotNull(bundleHandler.serviceComponentName)

    var amount: Amount
        get() = bundleHandler.amount ?: dropInConfiguration.amount
        private set(value) {
            bundleHandler.amount = value
        }

    internal var sessionDetails: SessionDetails?
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

    fun getPaymentMethods(): List<PaymentMethod> {
        return paymentMethodsApiResponse.paymentMethods.orEmpty()
    }

    fun getStoredPaymentMethods(): List<StoredPaymentMethod> {
        return paymentMethodsApiResponse.storedPaymentMethods.orEmpty()
    }

    fun shouldShowPreselectedStored(): Boolean {
        return getStoredPaymentMethods().any { it.isStoredPaymentSupported() } &&
            dropInConfiguration.showPreselectedStoredPaymentMethod
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
        val noStored = getStoredPaymentMethods().isEmpty()
        val singlePm = getPaymentMethods().size == 1

        val firstPaymentMethod = getPaymentMethods().firstOrNull()
        val paymentMethodHasComponent = firstPaymentMethod?.let {
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(it.type) &&
                !GooglePayComponent.PROVIDER.isPaymentMethodSupported(it) &&
                !PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(it.type)
        } ?: false

        return noStored && singlePm && paymentMethodHasComponent && dropInConfiguration.skipListWhenSinglePaymentMethod
    }

    fun onCreated() {
        navigateToInitialDestination()
        sendAnalyticsEvent()
    }

    fun onDropInServiceConnected() {
        val sessionModel = sessionDetails?.mapToModel()
        if (sessionModel == null) {
            Logger.d(TAG, "Session is null")
            return
        }

        val event = DropInActivityEvent.SessionServiceConnected(
            sessionModel = sessionModel,
            clientKey = dropInConfiguration.clientKey,
            environment = dropInConfiguration.environment,
            isFlowTakenOver = isSessionsFlowTakenOver
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

    private fun sendAnalyticsEvent() {
        Logger.v(TAG, "sendAnalyticsEvent")
        viewModelScope.launch {
            analyticsRepository.sendAnalyticsEvent()
        }
    }

    /**
     * @return the payment method details required to request the balance, or null if invalid
     */
    fun onBalanceCallRequested(giftCardComponentState: GiftCardComponentState): PaymentMethodDetails? {
        val paymentMethod = giftCardComponentState.data.paymentMethod
        if (paymentMethod == null) {
            Logger.e(TAG, "onBalanceCallRequested - paymentMethod is null")
            return null
        }
        cachedGiftCardComponentState = giftCardComponentState
        return paymentMethod
    }

    fun handleBalanceResult(balanceResult: BalanceResult): GiftCardBalanceResult {
        Logger.d(
            TAG,
            "handleBalanceResult - balance: ${balanceResult.balance} - " +
                "transactionLimit: ${balanceResult.transactionLimit}"
        )

        val giftCardBalanceResult = GiftCardBalanceUtils.checkBalance(
            balance = balanceResult.balance,
            transactionLimit = balanceResult.transactionLimit,
            amountToBePaid = amount
        )
        val cachedGiftCardComponentState =
            cachedGiftCardComponentState ?: throw CheckoutException("Failed to retrieved cached gift card object")
        return when (giftCardBalanceResult) {
            is GiftCardBalanceStatus.ZeroBalance -> {
                Logger.i(TAG, "handleBalanceResult - Gift Card has zero balance")
                GiftCardBalanceResult.Error(
                    R.string.checkout_giftcard_error_zero_balance,
                    "Gift Card has zero balance",
                    false
                )
            }
            is GiftCardBalanceStatus.NonMatchingCurrencies -> {
                Logger.e(TAG, "handleBalanceResult - Gift Card currency mismatch")
                GiftCardBalanceResult.Error(
                    R.string.checkout_giftcard_error_currency,
                    "Gift Card currency mismatch",
                    false
                )
            }
            is GiftCardBalanceStatus.ZeroAmountToBePaid -> {
                Logger.e(
                    TAG,
                    "handleBalanceResult - You must set an amount in DropInConfiguration.Builder to enable gift " +
                        "card payments"
                )
                GiftCardBalanceResult.Error(R.string.payment_failed, "Drop-in amount is not set", true)
            }
            is GiftCardBalanceStatus.FullPayment -> {
                cachedPartialPaymentAmount = giftCardBalanceResult.amountPaid
                GiftCardBalanceResult.FullPayment(
                    createGiftCardPaymentConfirmationData(giftCardBalanceResult, cachedGiftCardComponentState)
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
            shopperLocale = dropInConfiguration.shopperLocale,
            brand = giftCardComponentState.data.paymentMethod?.brand.orEmpty(),
            lastFourDigits = giftCardComponentState.lastFourDigits.orEmpty()
        )
    }

    fun handleOrderCreated(orderResponse: OrderResponse) {
        viewModelScope.launch(Dispatchers.IO) {
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
            amount = dropInConfiguration.amount
            Logger.d(TAG, "handleOrderResponse - Amount reverted: $amount")
            Logger.d(TAG, "handleOrderResponse - Order cancelled")
        } else {
            currentOrder = orderModel
            amount = orderModel.remainingAmount
            Logger.d(TAG, "handleOrderResponse - New amount set: $amount")
            Logger.d(TAG, "handleOrderResponse - Order cached")
        }
    }

    fun updatePaymentComponentStateForPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        // include amount value if merchant passed it to the DropIn
        val existingAmount = paymentComponentState.data.amount
        when {
            existingAmount != null && !existingAmount.isEmpty -> {
                Logger.d(TAG, "Payment amount already set: $existingAmount")
            }
            !amount.isEmpty -> {
                paymentComponentState.data.amount = amount
                Logger.d(TAG, "Payment amount set: $amount")
            }
            else -> {
                Logger.d(TAG, "Payment amount not set")
            }
        }
        currentOrder?.let {
            paymentComponentState.data.order = createOrder(it)
            Logger.d(TAG, "Order appended to payment")
        }
    }

    private fun createOrder(order: OrderModel): OrderRequest {
        return OrderRequest(
            pspReference = order.pspReference,
            orderData = order.orderData
        )
    }

    fun handlePaymentMethodsUpdate(paymentMethodsApiResponse: PaymentMethodsApiResponse, order: OrderResponse?) {
        viewModelScope.launch(Dispatchers.IO) {
            handleOrderResponse(order)
            this@DropInViewModel.paymentMethodsApiResponse = paymentMethodsApiResponse
            sendEvent(DropInActivityEvent.ShowPaymentMethods)
        }
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

        return orderStatusRepository.getOrderStatus(dropInConfiguration, orderResponse.orderData)
            .fold(
                onSuccess = { statusResponse ->
                    OrderModel(
                        orderData = orderResponse.orderData,
                        pspReference = orderResponse.pspReference,
                        remainingAmount = statusResponse.remainingAmount,
                        paymentMethods = statusResponse.paymentMethods
                    )
                },
                onFailure = { e ->
                    Logger.e(TAG, "Unable to fetch order details", e)
                    null
                }
            )
    }

    fun partialPaymentRequested() {
        val paymentComponentState = cachedGiftCardComponentState
            ?: throw CheckoutException("Lost reference to cached GiftCardComponentState")
        val partialPaymentAmount = cachedPartialPaymentAmount
            ?: throw CheckoutException("Lost reference to cached partial payment amount")
        paymentComponentState.data.amount = partialPaymentAmount
        Logger.d(TAG, "Partial payment amount set: $partialPaymentAmount")
        cachedGiftCardComponentState = null
        cachedPartialPaymentAmount = null
        sendEvent(DropInActivityEvent.MakePartialPayment(paymentComponentState))
    }

    fun orderCancellationRequested() {
        val order = currentOrder
            ?: throw CheckoutException("No order in progress")
        sendCancelOrderEvent(order, false)
    }

    fun cancelDropIn() {
        currentOrder?.let { sendCancelOrderEvent(it, true) }
        sendEvent(DropInActivityEvent.CancelDropIn)
    }

    private fun sendCancelOrderEvent(order: OrderModel, isDropInCancelledByUser: Boolean) {
        val orderRequest = OrderRequest(
            pspReference = order.pspReference,
            orderData = order.orderData
        )
        sendEvent(DropInActivityEvent.CancelOrder(orderRequest, isDropInCancelledByUser))
    }

    private fun sendEvent(event: DropInActivityEvent) {
        viewModelScope.launch {
            Logger.d(TAG, "sendEvent - ${event::class.simpleName}")
            eventChannel.send(event)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
