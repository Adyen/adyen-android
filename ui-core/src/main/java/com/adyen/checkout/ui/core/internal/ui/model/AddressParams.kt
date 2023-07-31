/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/2/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import androidx.annotation.RestrictTo

/**
 * Configuration class for Address Form in Address View. This class can be used define the
 * visibility of the address form.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class AddressParams {

    /**
     * Address Form will be hidden.
     */
    object None : AddressParams()

    /**
     * Only postal code will be shown as part of the card component.
     */
    data class PostalCode(
        val addressFieldPolicy: AddressFieldPolicy
    ) : AddressParams()

    /**
     * Full Address Form will be shown as part of the card component.
     *
     * @param defaultCountryCode Default country to be selected while initializing the form.
     * @param supportedCountryCodes Supported country codes to be filtered from the available country
     * options.
     */
    data class FullAddress(
        val defaultCountryCode: String? = null,
        val supportedCountryCodes: List<String> = emptyList(),
        val addressFieldPolicy: AddressFieldPolicy
    ) : AddressParams()
}

/**
 * Configuration for requirement of the address fields.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface AddressFieldPolicy
