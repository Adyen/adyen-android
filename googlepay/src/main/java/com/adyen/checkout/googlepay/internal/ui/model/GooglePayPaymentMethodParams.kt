/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.IssuerCountryCodes

/**
 * Resolved, non-nullable counterpart of the public
 * [com.adyen.checkout.googlepay.GooglePayPaymentMethodParameters]. Defaults are applied while
 * mapping in [GooglePayComponentParamsMapper] so that downstream code does not have to handle
 * nullability.
 */
internal sealed class GooglePayPaymentMethodParams {

    data class Card(
        val allowedAuthMethods: List<String>,
        val allowedCardNetworks: List<String>,
        val isAllowPrepaidCards: Boolean,
        val isAllowCreditCards: Boolean?,
        val issuerCountryCodes: IssuerCountryCodes?,
        val isAssuranceDetailsRequired: Boolean?,
        val isBillingAddressRequired: Boolean,
        val billingAddressParameters: BillingAddressParameters?,
    ) : GooglePayPaymentMethodParams()
}
