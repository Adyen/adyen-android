/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.ui.ManageFavoritesViewState.FavoriteListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ManageFavoritesViewModel(
    private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
) : ViewModel() {

    private val _viewState = MutableStateFlow(createInitialViewState())
    val viewState: StateFlow<ManageFavoritesViewState> = _viewState.asStateFlow()

    private fun createInitialViewState(): ManageFavoritesViewState {
        // TODO - check if we need to filter out unsupported payment methods
        val (cards, others) = paymentMethodsApiResponse.storedPaymentMethods.orEmpty()
            .partition { it.type == PaymentMethodTypes.SCHEME }

        return ManageFavoritesViewState(
            cards = cards.map { it.toFavoriteListItem() },
            others = others.map { it.toFavoriteListItem() },
        )
    }

    private fun StoredPaymentMethod.toFavoriteListItem(): FavoriteListItem {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> brand.orEmpty()
            else -> type.orEmpty()
        }

        val title: String = when (type) {
            PaymentMethodTypes.ACH -> "•••• ${bankAccountNumber?.takeLast(LAST_FOUR_LENGTH)}"
            PaymentMethodTypes.CASH_APP_PAY -> cashtag.orEmpty()
            PaymentMethodTypes.PAY_BY_BANK_US,
            PaymentMethodTypes.PAY_TO -> label.orEmpty()

            PaymentMethodTypes.SCHEME -> "•••• $lastFour"
            else -> name.orEmpty()
        }

        val subtitle = when (type) {
            PaymentMethodTypes.PAY_BY_BANK_US,
            PaymentMethodTypes.PAY_TO,
            PaymentMethodTypes.SCHEME -> name

            else -> null
        }

        return FavoriteListItem(
            id = id.orEmpty(),
            icon = icon,
            title = title,
            subtitle = subtitle,
        )
    }

    companion object {
        private const val LAST_FOUR_LENGTH = 4
    }

    class Factory(
        private val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ManageFavoritesViewModel(
                paymentMethodsApiResponse = paymentMethodsApiResponse,
            ) as T
        }
    }
}
