/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils

internal fun GooglePayComponentState.toPaymentComponentState(
    paymentMethodType: String?,
    sdkDataProvider: SdkDataProvider,
): GooglePayPaymentComponentState {
    if (paymentData == null) {
        return GooglePayPaymentComponentState(
            data = PaymentComponentData(
                paymentMethod = null,
                order = null,
            ),
            isValid = false,
        )
    }
    val googlePayDetails = GooglePayUtils.createGooglePayDetails(
        paymentData = paymentData,
        paymentMethodType = paymentMethodType,
        sdkData = sdkDataProvider.createEncodedSdkData(),
    )

    val paymentComponentData = PaymentComponentData(
        paymentMethod = googlePayDetails,
        order = null,
    )

    return GooglePayPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}
