/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/2/2023.
 */

package com.adyen.checkout.mbway

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import com.adyen.checkout.components.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId

/**
 * Represents the state of [MBWayComponent].
 */
data class MBWayComponentState(
    override val data: PaymentComponentData<MBWayPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<MBWayPaymentMethod>

// TODO: Write a test for this
internal fun MBWayDelegateState.toComponentState(
    analyticsManager: AnalyticsManager,
    fieldTransformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    order: OrderRequest?,
    amount: Amount?,
): MBWayComponentState {
    val sanitizedTelephoneNumber = fieldTransformerRegistry.transform(
        MBWayFieldId.LOCAL_PHONE_NUMBER,
        localPhoneNumberFieldState.value,
    )
    val telephoneNumber = "${countryCodeFieldState.value.callingCode}$sanitizedTelephoneNumber"

    val paymentMethod = MBWayPaymentMethod(
        type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
        checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
        telephoneNumber = telephoneNumber,
    )

    val paymentComponentData = PaymentComponentData(
        paymentMethod = paymentMethod,
        order = order,
        amount = amount,
    )

    return MBWayComponentState(
        data = paymentComponentData,
        isInputValid = isValid,
        isReady = true,
    )
}
