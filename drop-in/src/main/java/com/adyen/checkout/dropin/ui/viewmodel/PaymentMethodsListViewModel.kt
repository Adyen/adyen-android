/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/11/2020.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.checkPaymentMethodAvailability
import com.adyen.checkout.dropin.ui.order.OrderModel
import com.adyen.checkout.dropin.ui.paymentmethods.GiftCardPaymentMethodModel
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodHeader
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodModel
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodNote
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel
import com.adyen.checkout.dropin.ui.stored.makeStoredModel

@Suppress("TooManyFunctions")
class PaymentMethodsListViewModel(
    application: Application,
    private val paymentMethods: List<PaymentMethod>,
    storedPaymentMethods: List<StoredPaymentMethod>,
    private val order: OrderModel?,
    private val dropInConfiguration: DropInConfiguration,
    private val amount: Amount
) : AndroidViewModel(application), ComponentAvailableCallback<Configuration> {

    private val paymentMethodsMutableLiveData: MutableLiveData<List<PaymentMethodListItem>> = MutableLiveData()
    val paymentMethodsLiveData: LiveData<List<PaymentMethodListItem>> = paymentMethodsMutableLiveData

    private var availabilitySum = 0
    private var availabilitySkipSum = 0
    private var availabilityChecksum = 0

    private val storedPaymentMethodsList = mutableListOf<StoredPaymentMethodModel>()
    private val paymentMethodsList = mutableListOf<PaymentMethodModel>()
    private val orderPaymentMethodsList: List<GiftCardPaymentMethodModel> = setupOrderPaymentMethods(order)

    init {
        Logger.d(TAG, "initPaymentMethods")
        setupStoredPaymentMethods(storedPaymentMethods)
        setupPaymentMethods(paymentMethods)
    }

    fun getPaymentMethod(model: PaymentMethodModel): PaymentMethod {
        return paymentMethods[model.index]
    }

    private fun setupStoredPaymentMethods(storedPaymentMethods: List<StoredPaymentMethod>) {
        storedPaymentMethodsList.clear()
        for (storedPaymentMethod in storedPaymentMethods) {
            if (isStoredPaymentSupported(storedPaymentMethod)) {
                // We don't check for availability on stored payment methods
                storedPaymentMethodsList.add(
                    makeStoredModel(storedPaymentMethod, dropInConfiguration.isRemovingStoredPaymentMethodsEnabled)
                )
            } else {
                Logger.e(
                    TAG,
                    "Unsupported stored payment method - ${storedPaymentMethod.type} : ${storedPaymentMethod.name}"
                )
            }
        }
    }

    private fun isStoredPaymentSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return !storedPaymentMethod.type.isNullOrEmpty() &&
            !storedPaymentMethod.id.isNullOrEmpty() &&
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(storedPaymentMethod.type) &&
            storedPaymentMethod.isEcommerce
    }

    private fun setupPaymentMethods(paymentMethods: List<PaymentMethod>) {
        // We can't remove availability callbacks so just for safety let's crash in case of a concurrency issue.
        if (availabilityChecksum != 0) {
            throw CheckoutException(
                "Concurrency error. Cannot update Payment methods list because availability is still being checked."
            )
        }

        // Reset variables
        availabilitySum = 0
        availabilitySkipSum = 0
        availabilityChecksum = paymentMethods.size
        paymentMethodsList.clear()

        paymentMethods.forEachIndexed { index, paymentMethod ->
            val type = paymentMethod.type
            when {
                type == null -> {
                    throw CheckoutException("PaymentMethod type is null")
                }
                PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) -> {
                    Logger.v(TAG, "Supported payment method: $type")
                    // We assume payment method is available and remove it later when the callback comes
                    // this is the overwhelming majority of cases, and we keep the list ordered this way.
                    paymentMethodsList.add(paymentMethod.mapToModel(index))
                    checkPaymentMethodAvailability(getApplication(), paymentMethod, dropInConfiguration, amount, this)
                }
                else -> {
                    availabilitySkipSum++
                    if (PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(type)) {
                        Logger.e(TAG, "PaymentMethod not yet supported - $type")
                    } else {
                        Logger.d(TAG, "No details required - $type")
                        paymentMethodsList.add(paymentMethod.mapToModel(index))
                    }
                    // If last payment method is redirect list might be ready now
                    checkIfListIsReady()
                }
            }
        }
    }

    private fun PaymentMethod.mapToModel(index: Int): PaymentMethodModel {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> CARD_LOGO_TYPE
            PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GOOGLE_PAY_LOGO_TYPE
            PaymentMethodTypes.GIFTCARD -> brand
            else -> type
        }
        val drawIconBorder = icon != GOOGLE_PAY_LOGO_TYPE
        return PaymentMethodModel(
            index = index,
            type = type.orEmpty(),
            name = name.orEmpty(),
            icon = icon.orEmpty(),
            drawIconBorder = drawIconBorder
        )
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod, config: Configuration?) {
        Logger.d(TAG, "onAvailabilityResult - ${paymentMethod.type}: $isAvailable")

        availabilitySum++

        if (!isAvailable) {
            Logger.e(TAG, "${paymentMethod.type} NOT AVAILABLE")
            paymentMethodsList.removeAll { it.type == paymentMethod.type }
        }
        checkIfListIsReady()
    }

    private fun checkIfListIsReady() {
        // All payment methods availability have been returned or skipped.
        if ((availabilitySum + availabilitySkipSum) == availabilityChecksum) {
            // Reset variables
            availabilitySum = 0
            availabilitySkipSum = 0
            availabilityChecksum = 0

            onPaymentMethodsReady()
        }
    }

    private fun onPaymentMethodsReady() {
        Logger.d(TAG, "onPaymentMethodsReady: ${storedPaymentMethodsList.size} - ${paymentMethodsList.size}")
        paymentMethodsMutableLiveData.value = mutableListOf<PaymentMethodListItem>().apply {
            if (orderPaymentMethodsList.isNotEmpty()) {
                add(PaymentMethodHeader(PaymentMethodHeader.TYPE_GIFT_CARD_HEADER))
                addAll(orderPaymentMethodsList)
                order?.remainingAmount?.let { remainingAmount ->
                    val value = CurrencyUtils.formatAmount(remainingAmount, dropInConfiguration.shopperLocale)
                    add(
                        PaymentMethodNote(
                            getApplication<Application>().getString(
                                R.string.checkout_giftcard_pay_remaining_amount,
                                value
                            )
                        )
                    )
                }
            }
            if (storedPaymentMethodsList.isNotEmpty()) {
                add(PaymentMethodHeader(PaymentMethodHeader.TYPE_STORED_HEADER))
                addAll(storedPaymentMethodsList)
            }
            if (paymentMethodsList.isNotEmpty()) {
                val headerType =
                    if (storedPaymentMethodsList.isEmpty()) PaymentMethodHeader.TYPE_REGULAR_HEADER_WITHOUT_STORED
                    else PaymentMethodHeader.TYPE_REGULAR_HEADER_WITH_STORED
                add(PaymentMethodHeader(headerType))
                addAll(paymentMethodsList)
            }
        }
    }

    private fun setupOrderPaymentMethods(order: OrderModel?): List<GiftCardPaymentMethodModel> {
        return order?.paymentMethods.orEmpty().map {
            GiftCardPaymentMethodModel(
                imageId = it.type,
                lastFour = it.lastFour,
                amount = it.amount,
                transactionLimit = it.transactionLimit,
                shopperLocale = dropInConfiguration.shopperLocale
            )
        }
    }

    fun removePaymentMethodWithId(id: String) {
        removeStoredPaymentMethod(id)
        onPaymentMethodsReady()
    }

    private fun removeStoredPaymentMethod(id: String) {
        storedPaymentMethodsList.removeAll { it.id == id }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val CARD_LOGO_TYPE = "card"
        private const val GOOGLE_PAY_LOGO_TYPE = PaymentMethodTypes.GOOGLE_PAY
    }
}
