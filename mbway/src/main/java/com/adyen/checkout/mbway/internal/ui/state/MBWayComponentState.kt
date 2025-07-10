/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.data.OrderRequest
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformerRegistry
import com.adyen.checkout.core.components.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

// TODO - MBWayComponent in the comment should be changed to the new model
/**
 * Represents the state of [MBWayComponent].
 */
internal data class MBWayComponentState(
    override val data: PaymentComponentData<MBWayPaymentMethod>,
    override val isValid: Boolean,
) : PaymentComponentState<MBWayPaymentMethod>

internal fun MBWayDelegateState.toComponentState(
    checkoutAttemptId: String,
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
        checkoutAttemptId = checkoutAttemptId,
        telephoneNumber = telephoneNumber,
    )

    val paymentComponentData = PaymentComponentData(
        paymentMethod = paymentMethod,
        order = order,
        amount = amount,
    )

    return MBWayComponentState(
        data = paymentComponentData,
        isValid = isValid,
    )
}
