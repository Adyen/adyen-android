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
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import com.adyen.checkout.dropin.internal.ui.StoredPaymentMethodsViewState.StoredPaymentMethodsListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class StoredPaymentMethodsViewModel(
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {

    val viewState: StateFlow<StoredPaymentMethodsViewState> = paymentMethodRepository.storedPaymentMethods
        .map { createViewState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createViewState(emptyList()))

    private fun createViewState(storedPaymentMethods: List<StoredPaymentMethod>): StoredPaymentMethodsViewState {
        // TODO - check if we need to filter out unsupported payment methods
        val (cards, others) = storedPaymentMethods
            .partition { it.type == PaymentMethodTypes.SCHEME }

        return StoredPaymentMethodsViewState(
            cards = cards.map { it.toListItem() },
            others = others.map { it.toListItem() },
        )
    }

    private fun StoredPaymentMethod.toListItem(): StoredPaymentMethodsListItem {
        val icon = StoredPaymentMethodFormatter.getIcon(this)
        val title = StoredPaymentMethodFormatter.getTitle(this)
        val subtitle = StoredPaymentMethodFormatter.getSubtitle(this)

        return StoredPaymentMethodsListItem(
            id = id.orEmpty(),
            icon = icon,
            title = title,
            subtitle = subtitle,
        )
    }

    fun removeStoredPaymentMethod(id: String) {
        paymentMethodRepository.removeStoredPaymentMethod(id)
    }

    class Factory(
        private val paymentMethodRepository: PaymentMethodRepository,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return StoredPaymentMethodsViewModel(
                paymentMethodRepository = paymentMethodRepository,
            ) as T
        }
    }
}
