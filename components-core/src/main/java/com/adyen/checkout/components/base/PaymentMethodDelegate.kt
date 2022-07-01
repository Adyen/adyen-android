/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.components.base

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

/**
 * Handles all the logic in payment components
 */
interface PaymentMethodDelegate<
    ConfigurationT : Configuration,
    InputDataT : InputData,
    OutputDataT : OutputData,
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>
    > {
    fun getPaymentMethodType(): String

    fun onInputDataChanged(inputData: InputDataT)

    fun createComponentState(outputData: OutputDataT)
}
