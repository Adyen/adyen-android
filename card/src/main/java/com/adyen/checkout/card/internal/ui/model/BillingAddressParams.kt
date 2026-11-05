/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/11/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

internal sealed interface BillingAddressParams {

    val hideForCardBrands: Set<CardBrand>

    data object None : BillingAddressParams {
        override val hideForCardBrands: Set<CardBrand> = emptySet()
    }

    data class PostalCode(
        override val hideForCardBrands: Set<CardBrand>,
    ) : BillingAddressParams

    data class Full(
        val supportedCountryCodes: Set<String>,
        override val hideForCardBrands: Set<CardBrand>,
    ) : BillingAddressParams
}

internal fun BillingAddressParams.getPostalCodeRequirementPolicy(
    cardBrand: CardBrand? = null,
): RequirementPolicy = when (this) {
    is BillingAddressParams.PostalCode -> if (cardBrand in hideForCardBrands) {
        RequirementPolicy.Hidden
    } else {
        RequirementPolicy.Required
    }

    BillingAddressParams.None,
    is BillingAddressParams.Full -> RequirementPolicy.Hidden
}

internal fun BillingAddressParams.isBillingAddressVisible(
    cardBrand: CardBrand? = null,
): Boolean = when (this) {
    is BillingAddressParams.Full -> cardBrand !in hideForCardBrands
    BillingAddressParams.None,
    is BillingAddressParams.PostalCode -> false
}
