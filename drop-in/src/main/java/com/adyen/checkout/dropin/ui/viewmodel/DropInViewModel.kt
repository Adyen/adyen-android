/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.components.repository.OrderStatusRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.giftcard.GiftCardBalanceResult
import com.adyen.checkout.dropin.ui.giftcard.GiftCardPaymentConfirmationData
import com.adyen.checkout.dropin.ui.order.OrderModel
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.util.GiftCardBalanceStatus
import com.adyen.checkout.giftcard.util.GiftCardBalanceUtils
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.sessions.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class DropInViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val orderStatusRepository: OrderStatusRepository = OrderStatusRepository()
) : ViewModel() {

    private val eventChannel = Channel<DropInActivityEvent>(Channel.BUFFERED)
    internal val eventsFlow = eventChannel.receiveAsFlow()

    val dropInConfiguration: DropInConfiguration = getStateValueOrFail(DROP_IN_CONFIGURATION_KEY)

    val serviceComponentName: ComponentName = getStateValueOrFail(DROP_IN_SERVICE_KEY)

    var amount: Amount
        get() {
            return getStateValueOrFail(AMOUNT)
        }
        private set(value) {
            savedStateHandle[AMOUNT] = value
        }

    private var session: Session?
        get() = savedStateHandle[SESSION_KEY]
        private set(value) {
            savedStateHandle[SESSION_KEY] = value
        }

    private var isSessionsFlowTakenOver: Boolean
        get() = savedStateHandle[IS_SESSIONS_FLOW_TAKEN_OVER_KEY] ?: false
        private set(value) {
            savedStateHandle[IS_SESSIONS_FLOW_TAKEN_OVER_KEY] = value
        }

    private var paymentMethodsApiResponse: PaymentMethodsApiResponse?
        get() {
            return savedStateHandle[PAYMENT_METHODS_RESPONSE_KEY]
        }
        private set(value) {
            savedStateHandle[PAYMENT_METHODS_RESPONSE_KEY] = value
        }

    var isWaitingResult: Boolean
        get() {
            return savedStateHandle[IS_WAITING_FOR_RESULT_KEY] ?: false
        }
        set(value) {
            savedStateHandle[IS_WAITING_FOR_RESULT_KEY] = value
        }

    private var cachedGiftCardComponentState: GiftCardComponentState?
        get() {
            return savedStateHandle[CACHED_GIFT_CARD]
        }
        private set(value) {
            savedStateHandle[CACHED_GIFT_CARD] = value
        }

    private var cachedPartialPaymentAmount: Amount?
        get() {
            return savedStateHandle[PARTIAL_PAYMENT_AMOUNT]
        }
        private set(value) {
            savedStateHandle[PARTIAL_PAYMENT_AMOUNT] = value
        }

    var currentOrder: OrderModel?
        get() {
            return savedStateHandle[CURRENT_ORDER]
        }
        private set(value) {
            savedStateHandle[CURRENT_ORDER] = value
        }

    private fun <T> getStateValueOrFail(key: String): T {
        val value: T? = savedStateHandle[key]
        if (value == null) {
            Logger.e(TAG, "Failed to initialize bundle from SavedStateHandle")
            throw CheckoutException("Failed to initialize Drop-in, did you manually launch DropInActivity?")
        }
        return value
    }

    init {
        amount = dropInConfiguration.amount
    }

    fun getPaymentMethods(): List<PaymentMethod> {
        return paymentMethodsApiResponse?.paymentMethods.orEmpty()
    }

    fun getStoredPaymentMethods(): List<StoredPaymentMethod> {
        return paymentMethodsApiResponse?.storedPaymentMethods.orEmpty()
    }

    fun shouldShowPreselectedStored(): Boolean {
        return getStoredPaymentMethods().any { it.isEcommerce } &&
            dropInConfiguration.showPreselectedStoredPaymentMethod
    }

    fun getPreselectedStoredPaymentMethod(): StoredPaymentMethod {
        return getStoredPaymentMethods().firstOrNull {
            it.isEcommerce && PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(it.type)
        } ?: StoredPaymentMethod()
    }

    fun getStoredPaymentMethod(id: String): StoredPaymentMethod {
        return getStoredPaymentMethods().firstOrNull { it.id == id } ?: StoredPaymentMethod()
    }

    fun shouldSkipToSinglePaymentMethod(): Boolean {
        val noStored = getStoredPaymentMethods().isEmpty()
        val singlePm = getPaymentMethods().size == 1

        val firstPaymentMethod = getPaymentMethods().firstOrNull()
        val paymentMethodHasComponent =
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(firstPaymentMethod?.type) &&
                !GooglePayComponent.PAYMENT_METHOD_TYPES.contains(firstPaymentMethod?.type) &&
                !PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(firstPaymentMethod?.type)

        return noStored && singlePm && paymentMethodHasComponent && dropInConfiguration.skipListWhenSinglePaymentMethod
    }

    fun onCreated() {
        if (isInitializedWithSession() && paymentMethodsApiResponse == null) {
            // we just need to wait for the service to fetch the payment methods and send them to us
            // TODO add loading state?
            return
        } else {
            navigateToInitialDestination()
        }
    }

    fun onDropInServiceConnected() {
        val session = this.session
        if (session == null) {
            Logger.d(TAG, "Session is null")
        } else {
            val event = DropInActivityEvent.SessionServiceConnected(
                session = session,
                clientKey = dropInConfiguration.clientKey,
                baseUrl = dropInConfiguration.environment.baseUrl,
                shouldFetchPaymentMethods = paymentMethodsApiResponse == null,
                isFlowTakenOver = isSessionsFlowTakenOver
            )
            sendEvent(event)
        }
    }

    private fun isInitializedWithSession(): Boolean {
        return session != null
    }

    fun onSessionSetupSuccessful(paymentMethods: PaymentMethodsApiResponse?) {
        paymentMethodsApiResponse = paymentMethods
        navigateToInitialDestination()
    }

    fun onSessionDataChanged(sessionData: String) {
        session = session?.copy(sessionData = sessionData)
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
                if (currentOrder == null) GiftCardBalanceResult.RequestOrderCreation
                else GiftCardBalanceResult.RequestPartialPayment
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
            paymentMethodsApiResponse?.storedPaymentMethods = updatedStoredPaymentMethods
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

        private const val PAYMENT_METHODS_RESPONSE_KEY = "PAYMENT_METHODS_RESPONSE_KEY"
        private const val SESSION_KEY = "SESSION_KEY"
        private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
        private const val DROP_IN_CONFIGURATION_KEY = "DROP_IN_CONFIGURATION_KEY"
        private const val DROP_IN_SERVICE_KEY = "DROP_IN_SERVICE_KEY"
        private const val IS_WAITING_FOR_RESULT_KEY = "IS_WAITING_FOR_RESULT_KEY"
        private const val CACHED_GIFT_CARD = "CACHED_GIFT_CARD"
        private const val CURRENT_ORDER = "CURRENT_ORDER"
        private const val PARTIAL_PAYMENT_AMOUNT = "PARTIAL_PAYMENT_AMOUNT"
        private const val AMOUNT = "AMOUNT"

        fun putIntentExtras(
            intent: Intent,
            dropInConfiguration: DropInConfiguration,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            service: ComponentName,
        ) {
            intent.apply {
                putExtra(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodsApiResponse)
                putExtra(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
                putExtra(DROP_IN_SERVICE_KEY, service)
            }
        }

        fun putIntentExtras(
            intent: Intent,
            dropInConfiguration: DropInConfiguration,
            session: Session,
            service: ComponentName,
        ) {
            intent.apply {
                putExtra(SESSION_KEY, session)
                putExtra(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
                putExtra(DROP_IN_SERVICE_KEY, service)
            }
        }
    }
}
