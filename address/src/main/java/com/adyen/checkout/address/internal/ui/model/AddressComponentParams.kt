/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import java.util.Locale

/**
 * Configures the address component. Built by [com.adyen.checkout.address.internal.ui.AddressComponentFactory] from
 * what the host passes.
 *
 * @param shopperLocale The locale used to name the countries.
 * @param supportedCountryCodes The ISO codes of the countries the shopper can pick. When empty, every country can be
 * picked.
 */
internal data class AddressComponentParams(
    val shopperLocale: Locale,
    val supportedCountryCodes: Set<String>,
)
