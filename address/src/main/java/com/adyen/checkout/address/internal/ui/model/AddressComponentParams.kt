/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import java.util.Locale

/**
 * @param shopperLocale The locale of the shopper. It picks the default country and the language of the datasets.
 * @param supportedCountryCodes The ISO 3166-1 alpha-2 codes of the countries the shopper can pick. When empty, every
 * country can be picked.
 */
internal data class AddressComponentParams(
    val shopperLocale: Locale,
    val supportedCountryCodes: Set<String>,
)
