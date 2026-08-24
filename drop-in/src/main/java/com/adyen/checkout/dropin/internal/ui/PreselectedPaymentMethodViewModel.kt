/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.helper.StoredPaymentMethodFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.reflect.KClass

internal class PreselectedPaymentMethodViewModel(
    private val dropInParams: DropInParams,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val storedPaymentMethodId: String,
    private val navigator: DropInNavigator,
) : ViewModel() {

    private val _viewState = MutableStateFlow<PreselectedPaymentMethodViewState?>(null)
    val viewState: StateFlow<PreselectedPaymentMethodViewState?> = _viewState.asStateFlow()

    init {
        val storedPaymentMethod = paymentMethodRepository.storedPaymentMethods.value
            .firstOrNull { it.id == storedPaymentMethodId }

        if (storedPaymentMethod == null) {
            clearAndNavigateToPaymentMethodList()
        } else {
            _viewState.value = createInitialViewState(storedPaymentMethod)
        }
    }

    private fun createInitialViewState(storedPaymentMethod: StoredPaymentMethod): PreselectedPaymentMethodViewState {
        val formattedAmount = dropInParams.amount.format(dropInParams.shopperLocale)

        return PreselectedPaymentMethodViewState(
            logoTxVariant = StoredPaymentMethodFormatter.getIcon(storedPaymentMethod),
            title = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod),
            // TODO - Move to string resources after we support arguments
            subtitle = "Use ${storedPaymentMethod.name} to pay $formattedAmount",
            payButtonText = "Pay $formattedAmount",
        )
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onPayClicked() {
        val type = DropInPaymentFlowType.StoredPaymentMethod(storedPaymentMethodId)
        navigator.clearAndNavigateTo(PaymentMethodNavKey(type))
    }

    fun onOtherPaymentMethodClicked() {
        clearAndNavigateToPaymentMethodList()
    }

    private fun clearAndNavigateToPaymentMethodList() {
        navigator.clearAndNavigateTo(PaymentMethodListNavKey)
    }

    class Factory(
        private val dropInParams: DropInParams,
        private val storedPaymentMethodId: String,
        private val paymentMethodRepository: PaymentMethodRepository,
        private val navigator: DropInNavigator,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return PreselectedPaymentMethodViewModel(
                dropInParams = dropInParams,
                paymentMethodRepository = paymentMethodRepository,
                storedPaymentMethodId = storedPaymentMethodId,
                navigator = navigator,
            ) as T
        }
    }
}
