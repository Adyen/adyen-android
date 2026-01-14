/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.paymentmethod.BlikPaymentMethod

internal fun BlikComponentState.toPaymentComponentState(
    amount: Amount?,
    sdkDataProvider: SdkDataProvider,
): BlikPaymentComponentState {
    val paymentMethod = BlikPaymentMethod(
        type = BlikPaymentMethod.PAYMENT_METHOD_TYPE,
        sdkData = sdkDataProvider.createEncodedSdkData(),
        blikCode = blikCode.text,
        storedPaymentMethodId = null,
    )

    val paymentComponentData = PaymentComponentData(
        paymentMethod = paymentMethod,
        order = null,
        amount = amount,
    )

    return BlikPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}
