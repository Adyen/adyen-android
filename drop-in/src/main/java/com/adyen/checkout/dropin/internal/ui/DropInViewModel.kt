/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.internal.DropInResultContract
import com.adyen.checkout.dropin.internal.data.DefaultPaymentMethodRepository
import com.adyen.checkout.dropin.internal.data.PaymentMethodRepository
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.reflect.KClass

internal class DropInViewModel(
    private val input: DropInResultContract.Input,
) : ViewModel() {

    lateinit var dropInParams: DropInParams

    lateinit var paymentMethodRepository: PaymentMethodRepository

    val navigator: DropInNavigator = DropInNavigator()

    val checkoutContext = input.checkoutContext

    val dropInServiceManager = DropInServiceManager(input.serviceClass)

    val resultFlow: Flow<DropInResult> = merge(
        dropInServiceManager.paymentResultFlow.map { DropInResult.Completed(it) },
        dropInServiceManager.errorFlow.map { DropInResult.Failed(it.message ?: "Something went wrong") },
        navigator.finishFlow.filter { it }.map { DropInResult.Cancelled() },
    )

    init {
        initializePaymentMethods()
        initializeDropInParams()
        initializeBackStack()
    }

    private fun initializePaymentMethods() {
        val paymentMethods = when (val context = input.checkoutContext) {
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse
            is CheckoutContext.Advanced -> context.paymentMethodsApiResponse
        }

        if (paymentMethods == null) {
            // TODO - Return DropInResult.Failed and close drop-in
            return
        }

        paymentMethodRepository = DefaultPaymentMethodRepository(paymentMethods)
    }

    private fun initializeDropInParams() {
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
        val storedPaymentMethods = paymentMethodRepository.storedPaymentMethods.value
        val startingPoint = if (storedPaymentMethods.isEmpty()) {
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

    fun startDropInService(context: Context) {
        dropInServiceManager.startAndBind(context)
    }

    fun unbindDropInService(context: Context) {
        dropInServiceManager.unbind(context)
    }

    fun stopDropInService(context: Context) {
        dropInServiceManager.stop(context)
    }

    class Factory(
        private val inputProvider: () -> DropInResultContract.Input,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return DropInViewModel(
                input = inputProvider(),
            ) as T
        }
    }
}
