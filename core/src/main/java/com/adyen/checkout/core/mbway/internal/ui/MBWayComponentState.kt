/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import com.adyen.checkout.core.data.OrderRequest
import com.adyen.checkout.core.data.PaymentComponentData
import com.adyen.checkout.core.data.model.Amount
import com.adyen.checkout.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.core.internal.ui.state.transformer.FieldTransformerRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.paymentmethod.PaymentComponentState

// TODO - MBWayComponent in the comment should be changed to the new model
/**
 * Represents the state of [MBWayComponent].
 */
internal data class MBWayComponentState(
    override val data: PaymentComponentData<MBWayPaymentMethod>,
    override val isValid: Boolean,
) : PaymentComponentState<MBWayPaymentMethod>

internal fun MBWayDelegateState.toComponentState(
    analyticsManager: AnalyticsManager,
    fieldTransformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    order: OrderRequest?,
    amount: Amount?,
): MBWayComponentState {
    val sanitizedTelephoneNumber = fieldTransformerRegistry.transform(
        MBWayFieldId.PHONE_NUMBER,
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
        isValid = isValid
    )
}
