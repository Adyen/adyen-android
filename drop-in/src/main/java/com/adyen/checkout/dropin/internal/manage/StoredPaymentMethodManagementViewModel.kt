/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/12/2025.
 */

package com.adyen.checkout.dropin.internal.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.PaymentMethodSupportCheck
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class StoredPaymentMethodManagementViewModel(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodSupportCheck: PaymentMethodSupportCheck,
) : ViewModel() {

    val viewState: StateFlow<StoredPaymentMethodManagementViewState> = paymentMethodRepository.storedPaymentMethods
        .map { createViewState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createViewState(emptyList()))

    private fun createViewState(
        storedPaymentMethods: List<StoredPaymentMethod>
    ): StoredPaymentMethodManagementViewState {
        val (cards, others) = storedPaymentMethods
            .filter { paymentMethodSupportCheck.isSupported(it) }
            .partition { it.type == PaymentMethodTypes.SCHEME }

        return StoredPaymentMethodManagementViewState(
            cards = cards.map { it.toListItem() },
            others = others.map { it.toListItem() },
        )
    }

    private fun StoredPaymentMethod.toListItem(): StoredPaymentMethodManagementViewState.ListItem {
        val icon = StoredPaymentMethodFormatter.getIcon(this)
        val title = StoredPaymentMethodFormatter.getTitle(this)
        val subtitle = StoredPaymentMethodFormatter.getSubtitle(this)

        return StoredPaymentMethodManagementViewState.ListItem(
            id = id,
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
            return StoredPaymentMethodManagementViewModel(
                paymentMethodRepository = paymentMethodRepository,
                paymentMethodSupportCheck = PaymentMethodSupportCheck(),
            ) as T
        }
    }
}
