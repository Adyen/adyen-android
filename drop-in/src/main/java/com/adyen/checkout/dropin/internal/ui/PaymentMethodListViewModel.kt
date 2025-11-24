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
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PaymentMethodListViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
) : ViewModel() {

    private val _viewState = MutableStateFlow(createInitialViewState())
    val viewState: StateFlow<PaymentMethodListViewState> = _viewState.asStateFlow()

    private fun createInitialViewState(): PaymentMethodListViewState {
        val favoritesSection = paymentMethodsApiResponse.storedPaymentMethods?.let { paymentMethods ->
            FavoritesSection(
                options = paymentMethods
                    .filter { it.isSupported() }
                    .map { it.toPaymentMethodItem() },
            )
        }

        val paymentOptionsSection = paymentMethodsApiResponse.paymentMethods?.let { paymentMethods ->
            PaymentOptionsSection(
                // TODO - Change title if favorites section is not null
                title = CheckoutLocalizationKey.DROP_IN_PAYMENT_OPTIONS,
                options = paymentMethods
                    // TODO - Check availability for Google Pay and WeChat. If unavailable filter them also out
                    .filter { it.isSupported() }
                    .map { it.toPaymentMethodItem() },
            )
        }

        return PaymentMethodListViewState(
            amount = dropInParams.amount.format(dropInParams.shopperLocale),
            favoritesSection = favoritesSection,
            paymentOptionsSection = paymentOptionsSection,
        )
    }

    private fun StoredPaymentMethod.isSupported(): Boolean {
        return !type.isNullOrEmpty() &&
            !id.isNullOrEmpty() &&
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) &&
            isEcommerce
    }

    private fun StoredPaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> brand.orEmpty()
            else -> type.orEmpty()
        }

        return PaymentMethodItem(
            icon = icon,
            title = name.orEmpty(),
            subtitle = label,
        )
    }

    private fun PaymentMethod.isSupported(): Boolean {
        return !PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(type)
    }

    private fun PaymentMethod.toPaymentMethodItem(): PaymentMethodItem {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> CARD_LOGO
            PaymentMethodTypes.GIFTCARD -> brand.orEmpty()
            else -> type.orEmpty()
        }

        return PaymentMethodItem(
            icon = icon,
            title = name.orEmpty(),
        )
    }

    companion object {
        private const val CARD_LOGO = "card"
    }

    class Factory(
        private val dropInParams: DropInParams,
        private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodListViewModel(
                dropInParams = dropInParams,
                paymentMethodsApiResponse = paymentMethodsApiResponse,
            ) as T
        }
    }
}
