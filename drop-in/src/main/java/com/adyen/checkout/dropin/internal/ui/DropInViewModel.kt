/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.internal.DropInResultContract
import kotlin.reflect.KClass

internal class DropInViewModel(
    startingInput: DropInResultContract.Input?,
) : ViewModel() {

    private lateinit var input: DropInResultContract.Input

    private lateinit var paymentMethods: PaymentMethodsApiResponse

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf()

    val checkoutConfiguration: CheckoutConfiguration = input.checkoutContext.getCheckoutConfiguration()

    init {
        initializeInput(startingInput)
        initializePaymentMethods()
        initializeBackStack()
    }

    private fun initializeInput(startingInput: DropInResultContract.Input?) {
        if (startingInput == null) {
            // TODO - Return DropInResult.Failed and close drop-in
            return
        } else {
            input = startingInput
        }
    }

    private fun initializePaymentMethods() {
        val paymentMethods = when (val context = input.checkoutContext) {
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse
            is CheckoutContext.Advanced -> context.paymentMethodsApiResponse
            else -> null
        }

        if (paymentMethods == null) {
            // TODO - Return DropInResult.Failed and close drop-in
            return
        }
        this.paymentMethods = paymentMethods
    }

    private fun initializeBackStack() {
        val storedPaymentMethods = paymentMethods.storedPaymentMethods
        val startingPoint = if (storedPaymentMethods.isNullOrEmpty()) {
            PaymentMethodListNavKey
        } else {
            PreselectedPaymentMethodNavKey(storedPaymentMethods.first())
        }
        val startingEntries = listOf(EmptyNavKey, startingPoint)
        backStack.addAll(startingEntries)
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
                startingInput = inputProvider(),
            ) as T
        }
    }
}
