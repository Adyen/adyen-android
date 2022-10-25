/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/10/2022.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import android.app.Application
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.connection.OrderPaymentMethod
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
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
import com.adyen.checkout.dropin.ui.stored.mapStoredModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class PaymentMethodsListDelegate constructor(
    private val application: Application,
    private val paymentMethods: List<PaymentMethod>,
    private val storedPaymentMethods: List<StoredPaymentMethod>,
    private val order: OrderModel?,
    private val dropInConfiguration: DropInConfiguration,
    private val amount: Amount
) : ComponentAvailableCallback<Configuration> {

    private val _paymentMethodsFlow = MutableStateFlow<List<PaymentMethodListItem>>(emptyList())
    internal val paymentMethodsFlow: StateFlow<List<PaymentMethodListItem>> = _paymentMethodsFlow

    private lateinit var storedPaymentMethodsList: MutableList<StoredPaymentMethodModel>
    private var checkPaymentMethodsMap: HashMap<PaymentMethod, Boolean> = hashMapOf()

    init {
        setupPaymentMethods(paymentMethods)
    }

    internal fun getPaymentMethod(index: Int): PaymentMethod {
        return paymentMethods[index]
    }

    private fun setupPaymentMethods(paymentMethods: List<PaymentMethod>) {
        paymentMethods.forEach { paymentMethod ->
            val type = paymentMethod.type ?: throw IllegalStateException("PaymentMethod type is null")

            when {
                PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) -> {
                    Logger.d(TAG, "Supported payment method: $type")
                    checkPaymentMethodAvailability(application, paymentMethod, dropInConfiguration, amount, this)
                }
                PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(type) -> {
                    Logger.e(TAG, "PaymentMethod not yet supported - $type")
                    checkPaymentMethodsMap[paymentMethod] = false
                }
                else -> {
                    Logger.d(TAG, "No availability check required - $type")
                    checkPaymentMethodsMap[paymentMethod] = true
                }
            }
        }
        checkIfListReady()
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod, config: Configuration?) {
        Logger.d(TAG, "onAvailabilityResult - ${paymentMethod.type}: $isAvailable")
        checkPaymentMethodsMap[paymentMethod] = isAvailable
        checkIfListReady()
    }

    private fun checkIfListReady() {
        if (paymentMethods.size == checkPaymentMethodsMap.size) {
            populatePaymentMethods()
        }
    }

    private fun populatePaymentMethods() {
        val paymentMethods = mutableListOf<PaymentMethodListItem>().apply {
            // gift cards
            val giftCardsList: List<GiftCardPaymentMethodModel> =
                order?.paymentMethods?.mapToGiftCardPaymentMethodModel().orEmpty()
            if (giftCardsList.isNotEmpty()) {
                add(PaymentMethodHeader(PaymentMethodHeader.TYPE_GIFT_CARD_HEADER))
                addAll(giftCardsList)
            }
            // payment notes
            order?.remainingAmount?.let { remainingAmount ->
                val value = CurrencyUtils.formatAmount(remainingAmount, dropInConfiguration.shopperLocale)
                add(
                    PaymentMethodNote(application.getString(R.string.checkout_giftcard_pay_remaining_amount, value))
                )
            }
            // stored payment methods
            storedPaymentMethodsList = storedPaymentMethods.setupStoredPaymentMethods().toMutableList()
            val hasStoredPaymentMethods = storedPaymentMethodsList.isNotEmpty()
            if (hasStoredPaymentMethods) {
                add(PaymentMethodHeader(PaymentMethodHeader.TYPE_STORED_HEADER))
                addAll(storedPaymentMethodsList)
            }
            // payment methods
            val paymentMethodsList: List<PaymentMethodModel> = paymentMethods.setupPaymentMethodsList()
            if (paymentMethodsList.isNotEmpty()) {
                val headerType = if (hasStoredPaymentMethods)
                    PaymentMethodHeader.TYPE_REGULAR_HEADER_WITHOUT_STORED
                else
                    PaymentMethodHeader.TYPE_REGULAR_HEADER_WITH_STORED
                add(PaymentMethodHeader(headerType))
                addAll(paymentMethodsList)
            }
        }
        _paymentMethodsFlow.tryEmit(paymentMethods)
    }

    internal fun removePaymentMethodWithId(id: String) {
        if (this::storedPaymentMethodsList.isInitialized) {
            val item = storedPaymentMethodsList.first { item -> item.id == id }
            storedPaymentMethodsList.remove(item)
            populatePaymentMethods()
        } else throw IllegalStateException("store payment methods not found")
    }

    private fun List<PaymentMethod>.setupPaymentMethodsList(): List<PaymentMethodModel> =
        this.mapIndexedNotNull { index, paymentMethod ->
            val isAvailable = checkPaymentMethodsMap[paymentMethod]
                ?: throw IllegalStateException("payment method not found in map")
            if (isAvailable) {
                paymentMethod.mapToModel(index)
            } else
                null
        }

    private fun List<StoredPaymentMethod>.setupStoredPaymentMethods(): List<StoredPaymentMethodModel> =
        this.mapNotNull { storedPaymentMethod ->
            if (storedPaymentMethod.isStoredPaymentSupported())
                storedPaymentMethod.mapStoredModel(dropInConfiguration.isRemovingStoredPaymentMethodsEnabled)
            else
                null
        }

    private fun StoredPaymentMethod.isStoredPaymentSupported(): Boolean {
        return !this.type.isNullOrEmpty() &&
            !this.id.isNullOrEmpty() &&
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(this.type) &&
            this.isEcommerce
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

    private fun List<OrderPaymentMethod>.mapToGiftCardPaymentMethodModel(): List<GiftCardPaymentMethodModel> =
        this.map {
            GiftCardPaymentMethodModel(
                imageId = it.type,
                lastFour = it.lastFour,
                amount = it.amount,
                transactionLimit = it.transactionLimit,
                shopperLocale = dropInConfiguration.shopperLocale
            )
        }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val CARD_LOGO_TYPE = "card"
        private const val GOOGLE_PAY_LOGO_TYPE = PaymentMethodTypes.GOOGLE_PAY
    }
}
