/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.paymentmethod.MBWayDetails

internal fun MBWayComponentState.toPaymentComponentState(
    amount: Amount?,
    sdkDataProvider: SdkDataProvider,
): MBWayPaymentComponentState {
    val sanitizedPhoneNumber = phoneNumber.text.trimStart('0')
    val telephoneNumber = "${selectedCountryCode.callingCode}$sanitizedPhoneNumber"

    val mbWayDetails = MBWayDetails(
        type = MBWayDetails.PAYMENT_METHOD_TYPE,
        sdkData = sdkDataProvider.createEncodedSdkData(),
        telephoneNumber = telephoneNumber,
    )

    val paymentComponentData = PaymentComponentData(
        paymentMethod = mbWayDetails,
        order = null,
        amount = amount,
    )

    return MBWayPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}
