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
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.CheckoutConfiguration
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
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.internal.provider.mapToParams
import com.adyen.checkout.dropin.internal.ui.model.DropInActivityEvent
import com.adyen.checkout.dropin.internal.ui.model.DropInComponentParams
import com.adyen.checkout.dropin.internal.ui.model.DropInDestination
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
import com.adyen.checkout.ui.core.internal.ui.model.LookupAddress
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
) : ViewModel() {

    private val eventChannel: Channel<DropInActivityEvent> = bufferedChannel()
    internal val eventsFlow = eventChannel.receiveAsFlow()

    val checkoutConfiguration: CheckoutConfiguration = requireNotNull(bundleHandler.checkoutConfiguration)

    val dropInComponentParams: DropInComponentParams get() = checkoutConfiguration.mapToParams(amount)

    val serviceComponentName: ComponentName = requireNotNull(bundleHandler.serviceComponentName)

    var amount: Amount?
        get() = bundleHandler.amount
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

    private val _addressLookupOptionsFlow = bufferedChannel<List<LookupAddress>>()
    val addressLookupOptionsFlow: Flow<List<LookupAddress>> = _addressLookupOptionsFlow.receiveAsFlow()

    fun getPaymentMethods(): List<PaymentMethod> {
        return paymentMethodsApiResponse.paymentMethods.orEmpty()
    }

    fun getStoredPaymentMethods(): List<StoredPaymentMethod> {
        return paymentMethodsApiResponse.storedPaymentMethods.orEmpty()
    }

    fun shouldShowPreselectedStored(): Boolean {
        return getStoredPaymentMethods().any { it.isStoredPaymentSupported() } &&
            dropInComponentParams.showPreselectedStoredPaymentMethod
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
        if (!dropInComponentParams.skipListWhenSinglePaymentMethod) return false

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

    private fun getInitialAmount(): Amount? {
        return sessionDetails?.amount ?: checkoutConfiguration.amount
    }

    fun onCreated() {
        navigateToInitialDestination()
        setupAnalytics()
    }

    fun onDropInServiceConnected() {
        val sessionModel = sessionDetails?.mapToModel()
        if (sessionModel == null) {
            Logger.d(TAG, "Session is null")
            return
        }

        val event = DropInActivityEvent.SessionServiceConnected(
            sessionModel = sessionModel,
            clientKey = dropInComponentParams.clientKey,
            environment = dropInComponentParams.environment,
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
        Logger.v(TAG, "setupAnalytics")
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
            Logger.e(TAG, "onBalanceCallRequested - paymentMethod is null")
            return null
        }
        cachedGiftCardComponentState = giftCardComponentState
        return giftCardComponentState.data
    }

    fun handleBalanceResult(balanceResult: BalanceResult): GiftCardBalanceResult {
        Logger.d(
            TAG,
            "handleBalanceResult - balance: ${balanceResult.balance} - " +
                "transactionLimit: ${balanceResult.transactionLimit}",
        )

        val giftCardBalanceResult = GiftCardBalanceUtils.checkBalance(
            balance = balanceResult.balance,
            transactionLimit = balanceResult.transactionLimit,
            amountToBePaid = amount,
        )
        val cachedGiftCardComponentState =
            cachedGiftCardComponentState ?: throw CheckoutException("Failed to retrieved cached gift card object")
        return when (giftCardBalanceResult) {
            is GiftCardBalanceStatus.ZeroBalance -> {
                Logger.i(TAG, "handleBalanceResult - Gift Card has zero balance")
                GiftCardBalanceResult.Error(
                    R.string.checkout_giftcard_error_zero_balance,
                    "Gift Card has zero balance",
                    false,
                )
            }

            is GiftCardBalanceStatus.NonMatchingCurrencies -> {
                Logger.e(TAG, "handleBalanceResult - Gift Card currency mismatch")
                GiftCardBalanceResult.Error(
                    R.string.checkout_giftcard_error_currency,
                    "Gift Card currency mismatch",
                    false,
                )
            }

            is GiftCardBalanceStatus.ZeroAmountToBePaid -> {
                Logger.e(
                    TAG,
                    "handleBalanceResult - You must set an amount in DropInConfiguration.Builder to enable gift " +
                        "card payments",
                )
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
            shopperLocale = dropInComponentParams.shopperLocale,
            brand = giftCardComponentState.data.paymentMethod?.brand.orEmpty(),
            lastFourDigits = giftCardComponentState.lastFourDigits.orEmpty(),
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
            amount = getInitialAmount()
            Logger.d(TAG, "handleOrderResponse - Amount reverted: $amount")
            Logger.d(TAG, "handleOrderResponse - Order cancelled")
        } else {
            currentOrder = orderModel
            amount = orderModel.remainingAmount
            sessionDetails = sessionDetails?.copy(amount = orderModel.remainingAmount)
            Logger.d(TAG, "handleOrderResponse - New amount set: $amount")
            Logger.d(TAG, "handleOrderResponse - Order cached")
        }
    }

    fun updatePaymentComponentStateForPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        // include amount value if merchant passed it to the DropIn
        val existingAmount = paymentComponentState.data.amount
        when {
            existingAmount != null -> {
                Logger.d(TAG, "Payment amount already set: $existingAmount")
            }

            amount != null -> {
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
            orderData = order.orderData,
        )
    }

    fun handlePaymentMethodsUpdate(paymentMethodsApiResponse: PaymentMethodsApiResponse, order: OrderResponse?) {
        viewModelScope.launch(Dispatchers.IO) {
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

        return orderStatusRepository.getOrderStatus(dropInComponentParams.clientKey, orderResponse.orderData)
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
                    Logger.e(TAG, "Unable to fetch order details", e)
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

    fun onAddressLookupOptions(options: List<LookupAddress>) {
        Logger.d(TAG, "onAddressLookupOptions $options")
        viewModelScope.launch { _addressLookupOptionsFlow.send(options) }
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
            Logger.d(TAG, "sendEvent - ${event::class.simpleName}")
            eventChannel.send(event)
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
