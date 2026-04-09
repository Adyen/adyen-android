/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.PaymentResult
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PaymentMethodViewModel(
    private val paymentFlowType: DropInPaymentFlowType,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val checkoutContext: CheckoutContext,
    private val dropInServiceManager: DropInServiceManager,
    private val applicationContext: Context,
) : ViewModel() {

    private val _viewState = MutableStateFlow(createViewState())
    val viewState: StateFlow<PaymentMethodViewState> = _viewState.asStateFlow()

    private val checkoutCallbacks = CheckoutCallbacks(
        onSubmit = ::onSubmit,
        onAdditionalDetails = ::onAdditionalDetails,
        onError = ::onError,
        onFinished = ::onFinished,
    )

    val controller = CheckoutController(
        target = when (paymentFlowType) {
            is DropInPaymentFlowType.RegularPaymentMethod -> {
                CheckoutTarget.PaymentMethod(txVariant = paymentFlowType.txVariant)
            }

            is DropInPaymentFlowType.StoredPaymentMethod -> {
                CheckoutTarget.StoredPaymentMethod(id = paymentFlowType.id)
            }
        },
        context = checkoutContext,
        callbacks = checkoutCallbacks,
        applicationContext = applicationContext,
        coroutineScope = viewModelScope,
    )

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

    private suspend fun onSubmit(paymentComponentData: PaymentComponentData<*>): CheckoutResult {
        return dropInServiceManager.requestOnSubmit(paymentComponentData)
    }

    private suspend fun onAdditionalDetails(data: ActionComponentData): CheckoutResult {
        return dropInServiceManager.requestOnAdditionalDetails(data)
    }

    private fun onError(error: CheckoutError) {
        viewModelScope.launch {
            dropInServiceManager.onError(error)
        }
    }

    private fun onFinished(paymentResult: PaymentResult) {
        viewModelScope.launch {
            dropInServiceManager.onPaymentFinished(paymentResult)
        }
    }

    class Factory(
        private val paymentFlowType: DropInPaymentFlowType,
        private val paymentMethodRepository: PaymentMethodRepository,
        private val checkoutContext: CheckoutContext,
        private val dropInServiceManager: DropInServiceManager,
        private val applicationContext: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return PaymentMethodViewModel(
                paymentFlowType = paymentFlowType,
                paymentMethodRepository = paymentMethodRepository,
                checkoutContext = checkoutContext,
                dropInServiceManager = dropInServiceManager,
                applicationContext = applicationContext,
            ) as T
        }
    }
}
