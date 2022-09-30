/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

interface PayByBankDelegate : PaymentMethodDelegate, ViewProvidingDelegate {

    val outputDataFlow: Flow<PayByBankOutputData>

    val componentStateFlow: Flow<PaymentComponentState<PayByBankPaymentMethod>?>

    fun updateInputData(update: PayByBankInputData.() -> Unit)
}
