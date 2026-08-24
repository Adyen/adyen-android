/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PaymentMethodViewModel(
    private val paymentFlowType: DropInPaymentFlowType,
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {

    private val _viewState = MutableStateFlow(createViewState())
    val viewState: StateFlow<PaymentMethodViewState> = _viewState.asStateFlow()

    private fun createViewState(): PaymentMethodViewState {
        val paymentMethod = when (paymentFlowType) {
            is DropInPaymentFlowType.RegularPaymentMethod -> {
                paymentMethodRepository.paymentMethods.first { it.type == paymentFlowType.txVariant }
            }

            is DropInPaymentFlowType.StoredPaymentMethod -> {
                paymentMethodRepository.storedPaymentMethods.value.first { it.id == paymentFlowType.id }
            }
        }

        return PaymentMethodViewState(
            paymentMethodName = paymentMethod.name,
            description = paymentMethod.getDescription(),
        )
    }

    // TODO - Update this method once payment method refactor is done.
    //  Verify the localization for stored payment methods.
    private fun PaymentMethodResponse.getDescription(): CheckoutLocalizationKey? {
        return when (type) {
            PaymentMethodTypes.SCHEME -> CheckoutLocalizationKey.DROP_IN_PAYMENT_METHOD_CARD_DESCRIPTION
            else -> null
        }
    }

    class Factory(
        private val paymentFlowType: DropInPaymentFlowType,
        private val paymentMethodRepository: PaymentMethodRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodViewModel(
                paymentFlowType = paymentFlowType,
                paymentMethodRepository = paymentMethodRepository,
            ) as T
        }
    }
}
