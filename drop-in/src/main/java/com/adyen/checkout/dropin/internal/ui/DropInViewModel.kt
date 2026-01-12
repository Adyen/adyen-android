/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.dropin.internal.data.DefaultPaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

internal class DropInViewModel(
    input: DropInResultContract.Input?,
) : ViewModel() {

    // TODO - remove
    lateinit var paymentMethods: PaymentMethodsApiResponse

    lateinit var dropInParams: DropInParams

    val navigator: DropInNavigator = DropInNavigator()

    lateinit var paymentMethodRepository: PaymentMethodRepository

    init {
        if (verifyInput(input)) {
            initializePaymentMethods(input)
            initializeDropInParams(input)
            initializeBackStack()
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun verifyInput(input: DropInResultContract.Input?): Boolean {
        contract {
            returns(true) implies (input != null)
        }

        return if (input == null) {
            // TODO - Return DropInResult.Failed and close drop-in
            adyenLog(AdyenLogLevel.ERROR) { "Input is null. Closing drop-in with failed result." }
            false
        } else {
            true
        }
    }

    private fun initializePaymentMethods(input: DropInResultContract.Input) {
        val paymentMethods = when (val context = input.checkoutContext) {
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse
            is CheckoutContext.Advanced -> context.paymentMethodsApiResponse
        }

        if (paymentMethods == null) {
            // TODO - Return DropInResult.Failed and close drop-in
            return
        }

        paymentMethodRepository = DefaultPaymentMethodRepository(paymentMethods)
        this.paymentMethods = paymentMethods
    }

    private fun initializeDropInParams(input: DropInResultContract.Input) {
        try {
            val sessionParams = (input.checkoutContext as? CheckoutContext.Sessions?)?.checkoutSession?.let {
                SessionParamsFactory.create(it)
            }
            dropInParams = DropInParamsMapper().map(
                checkoutConfiguration = input.checkoutContext.getCheckoutConfiguration(),
                sessionParams = sessionParams,
            )
        } catch (e: IllegalStateException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Failed to create DropInParams" }
            // TODO - Return DropInResult.Failed and close drop-in
        }
    }

    private fun initializeBackStack() {
        val storedPaymentMethods = paymentMethods.storedPaymentMethods
        val startingPoint = if (storedPaymentMethods.isNullOrEmpty()) {
            PaymentMethodListNavKey
        } else {
            PreselectedPaymentMethodNavKey(storedPaymentMethods.first())
        }
        navigator.navigateTo(startingPoint)
    }

    private fun CheckoutContext.getCheckoutConfiguration(): CheckoutConfiguration {
        return when (this) {
            is CheckoutContext.Sessions -> checkoutConfiguration
            is CheckoutContext.Advanced -> checkoutConfiguration
        }
    }

    class Factory(
        private val inputProvider: () -> DropInResultContract.Input?,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return DropInViewModel(
                input = inputProvider(),
            ) as T
        }
    }
}
