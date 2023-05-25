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
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes

internal class StoredCashAppPayDelegate(val storedPaymentMethod: StoredPaymentMethod) : CashAppPayDelegate {

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun shouldCreateComponentStateOnInit(): Boolean = true

    override suspend fun initialize(outputData: CashAppPayOutputData?, onCashAppPayStateChanged: (CashAppPayState) -> Unit) {
        // no ops
    }

    override fun onCleared() {
        // no ops
    }

    override fun cashAppPayStateChanged(newState: CashAppPayState, outputData: CashAppPayOutputData?): CashAppPayStateChangedResult {
        return CashAppPayStateChangedResult.NoOps
    }

    override fun showStorePaymentField(): Boolean = false

    override fun requiresInput(): Boolean = false

    override fun createComponentState(outputData: CashAppPayOutputData?): GenericComponentState<CashAppPayPaymentMethod> {
        val cashAppPayPaymentMethod = CashAppPayPaymentMethod().apply {
            type = CashAppPayPaymentMethod.PAYMENT_METHOD_TYPE
            storedPaymentMethodId = storedPaymentMethod.id
        }
        val paymentComponentData = PaymentComponentData<CashAppPayPaymentMethod>().apply {
            paymentMethod = cashAppPayPaymentMethod
        }
        return GenericComponentState(
            paymentComponentData,
            true,
            true,
        )
    }

    override fun onInputDataChanged(inputData: CashAppPayInputData): CashAppPayOutputData = CashAppPayOutputData()

    override suspend fun submit(outputData: CashAppPayOutputData?) {
        // no ops
    }
}
