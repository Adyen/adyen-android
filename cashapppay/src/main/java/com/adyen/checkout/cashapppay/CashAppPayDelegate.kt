/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/5/2023.
 */

package com.adyen.checkout.cashapppay

import app.cash.paykit.core.CashAppPayState
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod

internal interface CashAppPayDelegate : PaymentMethodDelegate {

    fun shouldCreateComponentStateOnInit(): Boolean

    suspend fun initialize(outputData: CashAppPayOutputData?, onCashAppPayStateChanged: (CashAppPayState) -> Unit)

    fun onCleared()

    fun cashAppPayStateChanged(newState: CashAppPayState, outputData: CashAppPayOutputData?): CashAppPayStateChangedResult

    fun showStorePaymentField(): Boolean

    fun requiresInput(): Boolean

    fun createComponentState(outputData: CashAppPayOutputData?): GenericComponentState<CashAppPayPaymentMethod>

    fun onInputDataChanged(inputData: CashAppPayInputData): CashAppPayOutputData

    suspend fun submit(outputData: CashAppPayOutputData?)
}
