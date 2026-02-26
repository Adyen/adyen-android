/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodItem
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodListSection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class PaymentMethodListViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {

    val viewState: StateFlow<PaymentMethodListViewState> = paymentMethodRepository.storedPaymentMethods
        .map { createInitialViewState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createInitialViewState(emptyList()))

    private fun createInitialViewState(storedPaymentMethods: List<StoredPaymentMethod>): PaymentMethodListViewState {
        val storedPaymentMethodSection = storedPaymentMethods
            .filter { it.isSupported() }
            .takeIf { it.isNotEmpty() }
            ?.map { it.toPaymentMethodItem() }
            ?.let { paymentMethods ->
                PaymentMethodListSection(
                    title = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_TITLE,
                    action = CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_FAVORITES_SECTION_ACTION,
                    options = paymentMethods,
                )
            }

        val paymentOptionsSection = paymentMethodRepository.paymentMethods
            // TODO - Check availability for Google Pay and WeChat. If unavailable filter them also out
            .filter { it.isSupported() }
            .takeIf { it.isNotEmpty() }
            ?.map { it.toPaymentMethodItem() }
            ?.let { paymentMethods ->
                PaymentMethodListSection(
                    title = if (storedPaymentMethodSection == null) {
                        CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE
                    } else {
                        CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_LIST_PAYMENT_OPTIONS_SECTION_TITLE_WITH_FAVORITES
                    },
                    action = null,
                    options = paymentMethods,
                )
            }

        return PaymentMethodListViewState(
            amount = dropInParams.amount.format(dropInParams.shopperLocale),
            storedPaymentMethodSection = storedPaymentMethodSection,
            paymentOptionsSection = paymentOptionsSection,
        )
    }

    private fun StoredPaymentMethod.isSupported(): Boolean {
        return !id.isNullOrEmpty() &&
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) &&
            isEcommerce
    }

    private fun StoredPaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = StoredPaymentMethodFormatter.getIcon(this)
        val title = StoredPaymentMethodFormatter.getTitle(this)
        val subtitle = StoredPaymentMethodFormatter.getSubtitle(this)

        return PaymentMethodItem(
            id = id.orEmpty(),
            icon = icon,
            title = title,
            subtitle = subtitle,
        )
    }

    private fun PaymentMethod.isSupported(): Boolean {
        return !PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(type)
    }

    private fun PaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> CARD_LOGO
            PaymentMethodTypes.GIFTCARD -> brand.orEmpty()
            else -> type
        }

        return PaymentMethodItem(
            id = type,
            icon = icon,
            title = name,
        )
    }

    companion object {
        private const val CARD_LOGO = "card"
    }

    class Factory(
        private val dropInParams: DropInParams,
        private val paymentMethodRepository: PaymentMethodRepository,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodListViewModel(
                dropInParams = dropInParams,
                paymentMethodRepository = paymentMethodRepository,
            ) as T
        }
    }
}
