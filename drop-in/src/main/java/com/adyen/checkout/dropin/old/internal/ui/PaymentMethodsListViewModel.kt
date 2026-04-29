/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.data.model.OrderPaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.util.CurrencyUtils
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.old.internal.provider.checkPaymentMethodAvailability
import com.adyen.checkout.dropin.old.internal.ui.model.DropInParams
import com.adyen.checkout.dropin.old.internal.ui.model.GiftCardPaymentMethodModel
import com.adyen.checkout.dropin.old.internal.ui.model.OrderModel
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodHeader
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodListItem
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodModel
import com.adyen.checkout.dropin.old.internal.ui.model.PaymentMethodNote
import com.adyen.checkout.dropin.old.internal.ui.model.StoredPaymentMethodModel
import com.adyen.checkout.dropin.old.internal.util.isStoredPaymentSupported
import com.adyen.checkout.dropin.old.internal.util.mapStoredModel
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSBrandLogo
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem.LogoItem
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem.TextItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions", "LongParameterList")
internal class PaymentMethodsListViewModel(
    private val application: Application,
    private val paymentMethods: List<PaymentMethod>,
    storedPaymentMethods: List<StoredPaymentMethod>,
    private val order: OrderModel?,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val dropInParams: DropInParams,
    private val dropInOverrideParams: DropInOverrideParams,
) : ViewModel(), ComponentAvailableCallback, ComponentCallback<PaymentComponentState<*>> {

    private val _paymentMethodsFlow = MutableStateFlow<List<PaymentMethodListItem>>(emptyList())
    internal val paymentMethodsFlow: StateFlow<List<PaymentMethodListItem>> = _paymentMethodsFlow

    private var storedPaymentMethodsList: MutableList<StoredPaymentMethodModel>? = null
    private var paymentMethodsAvailabilityMap: HashMap<PaymentMethod, Boolean> = hashMapOf()

    private val eventsChannel: Channel<PaymentMethodListStoredEvent> = bufferedChannel()
    val eventsFlow: Flow<PaymentMethodListStoredEvent> = eventsChannel.receiveAsFlow()

    private var componentState: PaymentComponentState<*>? = null

    init {
        storedPaymentMethodsList = storedPaymentMethods.mapToStoredPaymentMethodsModelList().toMutableList()
        setupPaymentMethods(paymentMethods)
    }

    internal fun getPaymentMethod(paymentMethodModel: PaymentMethodModel): PaymentMethod {
        return paymentMethods[paymentMethodModel.index]
    }

    private fun setupPaymentMethods(paymentMethods: List<PaymentMethod>) {
        paymentMethods.forEach { paymentMethod ->
            val type = requireNotNull(paymentMethod.type) { "PaymentMethod type is null" }

            when {
                PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) -> {
                    adyenLog(AdyenLogLevel.DEBUG) { "Supported payment method: $type" }
                    checkPaymentMethodAvailability(
                        application = application,
                        paymentMethod = paymentMethod,
                        checkoutConfiguration = checkoutConfiguration,
                        dropInOverrideParams = dropInOverrideParams,
                        callback = this,
                    )
                }

                PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(type) -> {
                    adyenLog(AdyenLogLevel.ERROR) { "PaymentMethod not yet supported - $type" }
                    paymentMethodsAvailabilityMap[paymentMethod] = false
                }

                else -> {
                    adyenLog(AdyenLogLevel.DEBUG) { "No availability check required - $type" }
                    paymentMethodsAvailabilityMap[paymentMethod] = true
                }
            }
        }
        checkIfListReady()
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod) {
        adyenLog(AdyenLogLevel.DEBUG) { "onAvailabilityResult - ${paymentMethod.type}: $isAvailable" }
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
            val hasGiftCards = giftCardsList.isNotEmpty()
            // payment notes
            order?.remainingAmount?.let { remainingAmount ->
                val value = CurrencyUtils.formatAmount(remainingAmount, dropInParams.shopperLocale)
                add(
                    PaymentMethodNote(application.getString(R.string.checkout_giftcard_pay_remaining_amount, value)),
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
                } else if (hasGiftCards) {
                    PaymentMethodHeader.TYPE_REGULAR_HEADER_WITHOUT_STORED
                } else {
                    null
                }

                headerType?.let {
                    add(PaymentMethodHeader(it))
                }
                addAll(paymentMethodsList)
            }
        }
        _paymentMethodsFlow.tryEmit(paymentMethods)
    }

    internal fun removePaymentMethodWithId(id: String) {
        storedPaymentMethodsList?.removeAll { it.id == id }
        populatePaymentMethods()
    }

    override fun onSubmit(state: PaymentComponentState<*>) {
        // no ops
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        eventsChannel.trySend(PaymentMethodListStoredEvent.AdditionalDetails(actionComponentData))
    }

    override fun onError(componentError: ComponentError) {
        eventsChannel.trySend(PaymentMethodListStoredEvent.ShowError(componentError))
    }

    override fun onStateChanged(state: PaymentComponentState<*>) {
        componentState = state
    }

    fun onClickStoredItem(
        storedPaymentMethod: StoredPaymentMethod,
        storedPaymentMethodModel: StoredPaymentMethodModel
    ) {
        if (componentState?.isInputValid == true) {
            eventsChannel.trySend(
                PaymentMethodListStoredEvent.ShowConfirmationPopup(
                    storedPaymentMethod.name ?: "",
                    storedPaymentMethodModel,
                ),
            )
        } else {
            eventsChannel.trySend(PaymentMethodListStoredEvent.ShowStoredComponentDialog(storedPaymentMethod))
        }
    }

    fun onClickConfirmationButton() {
        val state = componentState ?: return
        if (componentState?.isValid == true) {
            eventsChannel.trySend(PaymentMethodListStoredEvent.RequestPaymentsCall(state))
        }
    }

    private fun List<PaymentMethod>.mapToPaymentMethodModelList(): List<PaymentMethodModel> =
        mapIndexedNotNull { index, paymentMethod ->
            val isAvailable = requireNotNull(paymentMethodsAvailabilityMap[paymentMethod]) {
                "payment method not found in map"
            }
            if (isAvailable) paymentMethod.mapToModel(index) else null
        }

    private fun List<StoredPaymentMethod>.mapToStoredPaymentMethodsModelList(): List<StoredPaymentMethodModel> =
        mapNotNull { storedPaymentMethod ->
            if (storedPaymentMethod.isStoredPaymentSupported()) {
                storedPaymentMethod.mapStoredModel(
                    dropInParams.isRemovingStoredPaymentMethodsEnabled,
                    dropInParams.environment,
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
            environment = dropInParams.environment,
            brandList = if (type == PaymentMethodTypes.PAY_BY_BANK_US) {
                makeBrandList()
            } else {
                emptyList()
            },
        )
    }

    private fun makeBrandList(): List<LogoTextItem> {
        return listOf(
            PayByBankUSBrandLogo.entries.map {
                LogoItem(
                    it.path,
                    dropInParams.environment,
                )
            },
            listOf(TextItem(R.string.checkout_plus)),
        ).flatten()
    }

    private fun List<OrderPaymentMethod>.mapToGiftCardPaymentMethodModel(): List<GiftCardPaymentMethodModel> =
        map {
            GiftCardPaymentMethodModel(
                imageId = it.type,
                lastFour = it.lastFour,
                amount = it.amount,
                transactionLimit = it.transactionLimit,
                shopperLocale = dropInParams.shopperLocale,
                environment = dropInParams.environment,
            )
        }

    companion object {
        private const val CARD_LOGO_TYPE = "card"
        private const val GOOGLE_PAY_LOGO_TYPE = PaymentMethodTypes.GOOGLE_PAY
    }
}

internal sealed class PaymentMethodListStoredEvent {
    class ShowStoredComponentDialog(val storedPaymentMethod: StoredPaymentMethod) : PaymentMethodListStoredEvent()

    class ShowConfirmationPopup(val paymentMethodName: String, val storedPaymentMethodModel: StoredPaymentMethodModel) :
        PaymentMethodListStoredEvent()

    data class RequestPaymentsCall(val state: PaymentComponentState<*>) : PaymentMethodListStoredEvent()

    data class ShowError(val componentError: ComponentError) : PaymentMethodListStoredEvent()

    data class AdditionalDetails(val data: ActionComponentData) : PaymentMethodListStoredEvent()
}
