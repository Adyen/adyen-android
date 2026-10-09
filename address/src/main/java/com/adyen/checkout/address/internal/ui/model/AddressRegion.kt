/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import com.adyen.checkout.address.internal.data.model.AddressItem

/**
 * A country or a state the shopper can pick.
 *
 * @param code The ISO 3166-1 alpha-2 code of a country, or the ISO 3166-2 subdivision code of a state. This is what
 * the address stores and sends.
 * @param name The display name, already localized by the datasets API.
 */
internal data class AddressRegion(
    val code: String,
    val name: String,
)

/** Drops entries without a code, which cannot be stored or sent, and falls back to the code for a missing name. */
internal fun List<AddressItem>.toAddressRegions(): List<AddressRegion> = mapNotNull { item ->
    item.id?.let { code -> AddressRegion(code = code, name = item.name ?: code) }
}
