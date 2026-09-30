/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.card.internal.ui.model

internal sealed interface BillingAddressParams {

    data object None : BillingAddressParams

    data object PostalCode : BillingAddressParams

    data class Full(
        val supportedCountryCodes: Set<String>,
    ) : BillingAddressParams
}
