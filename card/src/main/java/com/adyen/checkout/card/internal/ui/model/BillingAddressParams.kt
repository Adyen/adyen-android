/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/11/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.common.CardBrand

internal sealed interface BillingAddressParams {

    data object None : BillingAddressParams

    data class PostalCode(
        val hideForCardBrands: Set<CardBrand>,
    ) : BillingAddressParams

    data class Full(
        val supportedCountryCodes: Set<String>,
        val hideForCardBrands: Set<CardBrand>,
    ) : BillingAddressParams
}
