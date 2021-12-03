/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.Order
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.giftcard.GiftCardBalanceResult
import com.adyen.checkout.dropin.ui.giftcard.GiftCardPaymentConfirmationData
import com.adyen.checkout.giftcard.GiftCardComponentState
import com.adyen.checkout.giftcard.util.GiftCardBalanceStatus
import com.adyen.checkout.giftcard.util.GiftCardBalanceUtils
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayConfiguration

private val TAG = LogUtil.getTag()

private const val PAYMENT_METHODS_RESPONSE_KEY = "PAYMENT_METHODS_RESPONSE_KEY"
private const val DROP_IN_CONFIGURATION_KEY = "DROP_IN_CONFIGURATION_KEY"
private const val DROP_IN_RESULT_INTENT_KEY = "DROP_IN_RESULT_INTENT_KEY"
private const val IS_WAITING_FOR_RESULT_KEY = "IS_WAITING_FOR_RESULT_KEY"
private const val CACHED_GIFT_CARD = "CACHED_GIFT_CARD"
private const val CURRENT_ORDER = "CURRENT_ORDER"
private const val PARTIAL_PAYMENT_AMOUNT = "PARTIAL_PAYMENT_AMOUNT"
private const val AMOUNT = "AMOUNT"

@Suppress("TooManyFunctions")
class DropInViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val dropInConfiguration: DropInConfiguration = getStateValueOrFail(DROP_IN_CONFIGURATION_KEY)
    val resultHandlerIntent: Intent? = savedStateHandle[DROP_IN_RESULT_INTENT_KEY]

    var amount: Amount
        get() {
            return getStateValueOrFail(AMOUNT)
        }
        private set(value) {
            savedStateHandle[AMOUNT] = value
        }

    var paymentMethodsApiResponse: PaymentMethodsApiResponse
        get() {
            return getStateValueOrFail(PAYMENT_METHODS_RESPONSE_KEY)
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

    var cachedGiftCardComponentState: GiftCardComponentState?
        get() {
            return savedStateHandle.get<GiftCardComponentState>(CACHED_GIFT_CARD)
        }
        private set(value) {
            savedStateHandle[CACHED_GIFT_CARD] = value
        }

    private var cachedPartialPaymentAmount: Amount?
        get() {
            return savedStateHandle.get<Amount>(PARTIAL_PAYMENT_AMOUNT)
        }
        private set(value) {
            savedStateHandle[PARTIAL_PAYMENT_AMOUNT] = value
        }

    private var currentOrder: OrderResponse?
        get() {
            return savedStateHandle.get<OrderResponse>(CURRENT_ORDER)
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

    val showPreselectedStored = paymentMethodsApiResponse.storedPaymentMethods?.any { it.isEcommerce } == true &&
        dropInConfiguration.showPreselectedStoredPaymentMethod
    val preselectedStoredPayment = paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull {
        it.isEcommerce && PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(it.type)
    } ?: StoredPaymentMethod()

    fun getStoredPaymentMethod(id: String): StoredPaymentMethod {
        return paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull { it.id == id } ?: StoredPaymentMethod()
    }

    fun shouldSkipToSinglePaymentMethod(): Boolean {
        val noStored = paymentMethodsApiResponse.storedPaymentMethods.isNullOrEmpty()
        val singlePm = paymentMethodsApiResponse.paymentMethods?.size == 1

        val firstPaymentMethod = paymentMethodsApiResponse.paymentMethods?.firstOrNull()
        val paymentMethodHasComponent = PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(firstPaymentMethod?.type) &&
            !GooglePayComponent.PAYMENT_METHOD_TYPES.contains(firstPaymentMethod?.type) &&
            !PaymentMethodTypes.SUPPORTED_ACTION_ONLY_PAYMENT_METHODS.contains(firstPaymentMethod?.type)

        return noStored && singlePm && paymentMethodHasComponent && dropInConfiguration.skipListWhenSinglePaymentMethod
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
        Logger.d(TAG, "handleBalanceResult - balance: ${balanceResult.balance} - transactionLimit: ${balanceResult.transactionLimit}")

        val giftCardBalanceResult = GiftCardBalanceUtils.checkBalance(
            balance = balanceResult.balance,
            transactionLimit = balanceResult.transactionLimit,
            amountToBePaid = amount
        )
        val cachedGiftCardComponentState = cachedGiftCardComponentState ?: throw CheckoutException("Failed to retrieved cached gift card object")
        return when (giftCardBalanceResult) {
            is GiftCardBalanceStatus.ZeroBalance -> {
                Logger.i(TAG, "handleBalanceResult - Gift Card has zero balance")
                GiftCardBalanceResult.Error(R.string.checkout_giftcard_error_zero_balance, "Gift Card has zero balance", false)
            }
            is GiftCardBalanceStatus.NonMatchingCurrencies -> {
                Logger.e(TAG, "handleBalanceResult - Gift Card currency mismatch")
                GiftCardBalanceResult.Error(R.string.checkout_giftcard_error_currency, "Gift Card currency mismatch", false)
            }
            is GiftCardBalanceStatus.ZeroAmountToBePaid -> {
                Logger.e(TAG, "handleBalanceResult - You must set an amount in DropInConfiguration.Builder to enable gift card payments")
                GiftCardBalanceResult.Error(R.string.payment_failed, "Drop-in amount is not set", true)
            }
            is GiftCardBalanceStatus.FullPayment -> {
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

    fun handleOrderResponse(orderResponse: OrderResponse?) {
        if (orderResponse == null) {
            currentOrder = null
            amount = dropInConfiguration.amount
            Logger.d(TAG, "handleOrderResponse - Amount reverted: $amount")
            Logger.d(TAG, "handleOrderResponse - Order cancelled")
        } else {
            currentOrder = orderResponse
            amount = orderResponse.remainingAmount ?: throw CheckoutException("Provided order does not have a remainingAmount")
            Logger.d(TAG, "handleOrderResponse - New amount set: $amount")
            Logger.d(TAG, "handleOrderResponse - Order cached")
        }
    }

    fun updatePaymentComponentStateForPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        // include amount value if merchant passed it to the DropIn
        val amount = when {
            cachedPartialPaymentAmount != null -> cachedPartialPaymentAmount
            !amount.isEmpty -> amount
            else -> null
        }
        cachedPartialPaymentAmount = null
        amount?.let {
            paymentComponentState.data.amount = it
            Logger.d(TAG, "Payment amount set: $it")
        }
        currentOrder?.let {
            paymentComponentState.data.order = createOrder(it)
            Logger.d(TAG, "Order appended to payment")
        }
    }

    private fun createOrder(orderResponse: OrderResponse): Order {
        return Order(
            pspReference = orderResponse.pspReference,
            orderData = orderResponse.orderData
        )
    }

    fun handlePaymentMethodsUpdate(paymentMethodsApiResponse: PaymentMethodsApiResponse, order: OrderResponse?) {
        handleOrderResponse(order)
        this.paymentMethodsApiResponse = paymentMethodsApiResponse
    }

    fun updateGooglePayConfiguration(googlePayConfiguration: GooglePayConfiguration): GooglePayConfiguration {
        if (currentOrder == null) return googlePayConfiguration
        return GooglePayConfiguration.Builder(googlePayConfiguration).setAmount(amount).build()
    }

    companion object {
        fun putIntentExtras(
            intent: Intent,
            dropInConfiguration: DropInConfiguration,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            resultHandlerIntent: Intent?
        ) {
            intent.apply {
                putExtra(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodsApiResponse)
                putExtra(DROP_IN_CONFIGURATION_KEY, dropInConfiguration)
                putExtra(DROP_IN_RESULT_INTENT_KEY, resultHandlerIntent)
            }
        }
    }
}
