/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import com.adyen.checkout.components.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel

internal data class MBWayDelegateState(
    val countries: List<CountryModel>,
    val countryCodeFieldState: ComponentFieldDelegateState<CountryModel>,
    val localPhoneNumberFieldState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
) {
    val isValid: Boolean
        get() = countryCodeFieldState.validation?.isValid() == true &&
            localPhoneNumberFieldState.validation?.isValid() == true
}

internal fun MBWayDelegateState.toViewState() = MBWayViewState(
    countries = this.countries,
    countryCodeFieldState = this.countryCodeFieldState.toComponentFieldViewState(),
    phoneNumberFieldState = this.localPhoneNumberFieldState.toComponentFieldViewState(),
)

internal fun <T> ComponentFieldDelegateState<T>.toComponentFieldViewState() =
    ComponentFieldViewState(
        value = value,
        hasFocus = hasFocus,
        errorMessageId = takeIf { fieldState ->
            fieldState.shouldShowValidationError()
        }?.validation.let { it as? Validation.Invalid }?.reason,
    )

internal fun <T> ComponentFieldDelegateState<T>.shouldShowValidationError() =
    !this.hasFocus || this.shouldHighlightValidationError

internal fun MBWayDelegateState.toComponentState(
    analyticsManager: AnalyticsManager,
    fieldTransformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    order: OrderRequest?,
    amount: Amount?,
): MBWayComponentState {
    val paymentMethod = MBWayPaymentMethod(
        type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
        checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
        telephoneNumber = fieldTransformerRegistry.transform(
            MBWayFieldId.LOCAL_PHONE_NUMBER,
            localPhoneNumberFieldState.value,
        ),
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
