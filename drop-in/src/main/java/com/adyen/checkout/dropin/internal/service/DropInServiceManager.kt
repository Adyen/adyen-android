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
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.dropin.DropInService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class DropInServiceManager(
    private val serviceClass: Class<out DropInService>,
) {

    private val _paymentResultFlow = MutableSharedFlow<CheckoutResultCode>()
    val paymentResultFlow: SharedFlow<CheckoutResultCode> = _paymentResultFlow.asSharedFlow()

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

    suspend fun requestOnSubmit(paymentComponentData: PaymentComponentData<*>): SubmitResult {
        return DropInServiceRegistry.awaitService().onSubmit(paymentComponentData)
    }

    suspend fun requestOnAdditionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        return DropInServiceRegistry.awaitService().onAdditionalDetails(data)
    }

    suspend fun onPaymentCompleted(resultCode: CheckoutResultCode) {
        _paymentResultFlow.emit(resultCode)
    }

    suspend fun onFailure(error: CheckoutError) {
        _errorFlow.emit(error)
    }
}
