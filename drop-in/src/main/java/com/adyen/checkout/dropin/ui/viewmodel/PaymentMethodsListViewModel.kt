/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/11/2020.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.data.model.OrderPaymentMethod
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
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
import com.adyen.checkout.dropin.ui.stored.isStoredPaymentSupported
import com.adyen.checkout.dropin.ui.stored.mapStoredModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class PaymentMethodsListViewModel(
    private val application: Application,
    private val paymentMethods: List<PaymentMethod>,
    storedPaymentMethods: List<StoredPaymentMethod>,
    private val order: OrderModel?,
    private val dropInConfiguration: DropInConfiguration,
    private val amount: Amount
) : ViewModel(), ComponentAvailableCallback {

    private val _paymentMethodsFlow = MutableStateFlow<List<PaymentMethodListItem>>(emptyList())
    internal val paymentMethodsFlow: StateFlow<List<PaymentMethodListItem>> = _paymentMethodsFlow

    private var storedPaymentMethodsList: MutableList<StoredPaymentMethodModel>? = null
    private var paymentMethodsAvailabilityMap: HashMap<PaymentMethod, Boolean> = hashMapOf()

    init {
        storedPaymentMethodsList = storedPaymentMethods.mapToStoredPaymentMethodsModelList().toMutableList()
        setupPaymentMethods(paymentMethods)
    }

    internal fun getPaymentMethod(paymentMethodModel: PaymentMethodModel): PaymentMethod {
        return paymentMethods[paymentMethodModel.index]
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
                    paymentMethodsAvailabilityMap[paymentMethod] = false
                }
                else -> {
                    Logger.d(TAG, "No availability check required - $type")
                    paymentMethodsAvailabilityMap[paymentMethod] = true
                }
            }
        }
        checkIfListReady()
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod) {
        Logger.d(TAG, "onAvailabilityResult - ${paymentMethod.type}: $isAvailable")
        paymentMethodsAvailabilityMap[paymentMethod] = isAvailable
        checkIfListReady()
    }

    private fun checkIfListReady() {
        if (paymentMethods.size == paymentMethodsAvailabilityMap.size) {
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
            val hasStoredPaymentMethods = storedPaymentMethodsList?.isNotEmpty() ?: false
            if (hasStoredPaymentMethods) {
                storedPaymentMethodsList?.let {
                    add(PaymentMethodHeader(PaymentMethodHeader.TYPE_STORED_HEADER))
                    addAll(it)
                }
            }
            // payment methods
            val paymentMethodsList: List<PaymentMethodModel> = paymentMethods.mapToPaymentMethodModelList()
            if (paymentMethodsList.isNotEmpty()) {
                val headerType = if (hasStoredPaymentMethods) {
                    PaymentMethodHeader.TYPE_REGULAR_HEADER_WITH_STORED
                } else {
                    PaymentMethodHeader.TYPE_REGULAR_HEADER_WITHOUT_STORED
                }

                add(PaymentMethodHeader(headerType))
                addAll(paymentMethodsList)
            }
        }
        _paymentMethodsFlow.tryEmit(paymentMethods)
    }

    internal fun removePaymentMethodWithId(id: String) {
        storedPaymentMethodsList?.removeAll { it.id == id }
        populatePaymentMethods()
    }

    private fun List<PaymentMethod>.mapToPaymentMethodModelList(): List<PaymentMethodModel> =
        mapIndexedNotNull { index, paymentMethod ->
            val isAvailable = paymentMethodsAvailabilityMap[paymentMethod]
                ?: throw IllegalStateException("payment method not found in map")
            if (isAvailable) paymentMethod.mapToModel(index) else null
        }

    private fun List<StoredPaymentMethod>.mapToStoredPaymentMethodsModelList(): List<StoredPaymentMethodModel> =
        mapNotNull { storedPaymentMethod ->
            if (storedPaymentMethod.isStoredPaymentSupported()) {
                storedPaymentMethod.mapStoredModel(
                    dropInConfiguration.isRemovingStoredPaymentMethodsEnabled,
                    dropInConfiguration.environment,
                )
            } else {
                null
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
            drawIconBorder = drawIconBorder,
            environment = dropInConfiguration.environment,
        )
    }

    private fun List<OrderPaymentMethod>.mapToGiftCardPaymentMethodModel(): List<GiftCardPaymentMethodModel> =
        map {
            GiftCardPaymentMethodModel(
                imageId = it.type,
                lastFour = it.lastFour,
                amount = it.amount,
                transactionLimit = it.transactionLimit,
                shopperLocale = dropInConfiguration.shopperLocale,
                environment = dropInConfiguration.environment,
            )
        }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val CARD_LOGO_TYPE = "card"
        private const val GOOGLE_PAY_LOGO_TYPE = PaymentMethodTypes.GOOGLE_PAY
    }
}
