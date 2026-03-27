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
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PayByBankUSPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodItem
import com.adyen.checkout.dropin.internal.ui.PaymentMethodListViewState.PaymentMethodListSection
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSBrandLogo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class PaymentMethodListViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodSupportCheck: PaymentMethodSupportCheck,
) : ViewModel() {

    val viewState: StateFlow<PaymentMethodListViewState> = paymentMethodRepository.storedPaymentMethods
        .map { createInitialViewState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createInitialViewState(emptyList()))

    private fun createInitialViewState(storedPaymentMethods: List<StoredPaymentMethod>): PaymentMethodListViewState {
        val storedPaymentMethodSection = storedPaymentMethods
            .filter { paymentMethodSupportCheck.isSupported(it) }
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
            .filter { paymentMethodSupportCheck.isSupported(it) }
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

    private fun StoredPaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = StoredPaymentMethodFormatter.getIcon(this)
        val title = StoredPaymentMethodFormatter.getTitle(this)
        val subtitle = StoredPaymentMethodFormatter.getSubtitle(this)

        return PaymentMethodItem(
            id = id,
            icon = icon,
            title = title,
            subtitle = subtitle,
        )
    }

    private fun PaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = when (this) {
            is CardPaymentMethod -> CARD_LOGO
            is GiftCardPaymentMethod -> brand
            else -> type
        }

        val brands = when (this) {
            is CardPaymentMethod -> brands
            is PayByBankUSPaymentMethod -> PayByBankUSBrandLogo.entries.map { it.path }
            else -> null
        }

        return PaymentMethodItem(
            id = type,
            icon = icon,
            title = name,
            brands = brands,
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
                paymentMethodSupportCheck = PaymentMethodSupportCheck(),
            ) as T
        }
    }
}
