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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.ui.ManageFavoritesViewState.FavoriteListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ManageFavoritesViewModel(
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {

    val viewState: StateFlow<ManageFavoritesViewState> = paymentMethodRepository.favorites.map { favorites ->
        createViewState(favorites)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createViewState(emptyList()))

    private fun createViewState(favorites: List<StoredPaymentMethod>): ManageFavoritesViewState {
        // TODO - check if we need to filter out unsupported payment methods
        val (cards, others) = favorites
            .partition { it.type == PaymentMethodTypes.SCHEME }

        return ManageFavoritesViewState(
            cards = cards.map { it.toFavoriteListItem() },
            others = others.map { it.toFavoriteListItem() },
        )
    }

    private fun StoredPaymentMethod.toFavoriteListItem(): FavoriteListItem {
        val icon = when (type) {
            PaymentMethodTypes.SCHEME -> brand.orEmpty()
            else -> type
        }

        val title: String = when (type) {
            PaymentMethodTypes.ACH -> "•••• ${bankAccountNumber?.takeLast(LAST_FOUR_LENGTH).orEmpty()}"
            PaymentMethodTypes.CASH_APP_PAY -> cashtag.orEmpty()
            PaymentMethodTypes.PAY_BY_BANK_US,
            PaymentMethodTypes.PAY_TO -> label.orEmpty()

            PaymentMethodTypes.SCHEME -> "•••• ${lastFour.orEmpty()}"
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

    fun removeFavorite(id: String) {
        paymentMethodRepository.removeFavorite(id)
    }

    companion object {
        private const val LAST_FOUR_LENGTH = 4
    }

    class Factory(
        private val paymentMethodRepository: PaymentMethodRepository,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ManageFavoritesViewModel(
                paymentMethodRepository = paymentMethodRepository,
            ) as T
        }
    }
}
