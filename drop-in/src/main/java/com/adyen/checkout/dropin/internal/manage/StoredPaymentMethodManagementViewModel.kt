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
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodExpiryCheck
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class StoredPaymentMethodManagementViewModel(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodSupportCheck: PaymentMethodSupportCheck,
    private val expiryCheck: StoredPaymentMethodExpiryCheck,
) : ViewModel() {

    private val showRemovalError = MutableStateFlow(false)

    val viewState: StateFlow<StoredPaymentMethodManagementViewState> = combine(
        paymentMethodRepository.storedPaymentMethods,
        paymentMethodRepository.storedPaymentMethodIdsBeingRemoved,
        showRemovalError,
        ::createViewState,
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        createViewState(emptyList(), emptySet(), false),
    )

    init {
        paymentMethodRepository.storedPaymentMethodRemovalFailures
            .onEach { showRemovalError.value = true }
            .launchIn(viewModelScope)
    }

    private fun createViewState(
        storedPaymentMethods: List<StoredPaymentMethod>,
        idsBeingRemoved: Set<String>,
        showRemovalError: Boolean,
    ): StoredPaymentMethodManagementViewState {
        val (cards, others) = storedPaymentMethods
            .filter { paymentMethodSupportCheck.isSupported(it) }
            .partition { it.type == PaymentMethodTypes.SCHEME }

        return StoredPaymentMethodManagementViewState(
            cards = cards.map { it.toListItem(isRemoving = it.id in idsBeingRemoved) },
            others = others.map { it.toListItem(isRemoving = it.id in idsBeingRemoved) },
            showOthersSectionTitle = cards.isNotEmpty(),
            showRemovalError = showRemovalError,
        )
    }

    private fun StoredPaymentMethod.toListItem(isRemoving: Boolean): StoredPaymentMethodManagementViewState.ListItem {
        val icon = StoredPaymentMethodFormatter.getIcon(this)
        val title = StoredPaymentMethodFormatter.getTitle(this)
        val subtitle = StoredPaymentMethodFormatter.getSubtitle(this)

        return StoredPaymentMethodManagementViewState.ListItem(
            id = id,
            icon = icon,
            title = title,
            subtitle = subtitle,
            isExpired = expiryCheck.isExpired(this),
            removalTitle = StoredPaymentMethodFormatter.getRemovalTitle(this),
            isRemoving = isRemoving,
        )
    }

    fun removeStoredPaymentMethod(id: String) {
        showRemovalError.value = false
        viewModelScope.launch { paymentMethodRepository.removeStoredPaymentMethod(id) }
    }

    class Factory(
        private val paymentMethodRepository: PaymentMethodRepository,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return StoredPaymentMethodManagementViewModel(
                paymentMethodRepository = paymentMethodRepository,
                paymentMethodSupportCheck = PaymentMethodSupportCheck(),
                expiryCheck = StoredPaymentMethodExpiryCheck.DEFAULT,
            ) as T
        }
    }
}
