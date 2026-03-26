/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/1/2026.
 */

package com.adyen.checkout.dropin.internal.service

import android.content.Context
import android.content.Intent
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.PaymentResult
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.dropin.DropInService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class DropInServiceManager(
    private val serviceClass: Class<out DropInService>,
) {

    private val _paymentResultFlow = MutableSharedFlow<PaymentResult>()
    val paymentResultFlow: SharedFlow<PaymentResult> = _paymentResultFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<CheckoutError>()
    val errorFlow: SharedFlow<CheckoutError> = _errorFlow.asSharedFlow()

    fun start(context: Context) {
        val intent = Intent(context, serviceClass)
        context.startService(intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, serviceClass)
        context.stopService(intent)
    }

    suspend fun requestOnSubmit(paymentComponentData: PaymentComponentData<*>): CheckoutResult {
        return DropInServiceRegistry.awaitService().onSubmit(paymentComponentData)
    }

    suspend fun requestOnAdditionalDetails(data: ActionComponentData): CheckoutResult {
        return DropInServiceRegistry.awaitService().onAdditionalDetails(data)
    }

    suspend fun onPaymentFinished(paymentResult: PaymentResult) {
        _paymentResultFlow.emit(paymentResult)
    }

    suspend fun onError(error: CheckoutError) {
        _errorFlow.emit(error)
    }
}
