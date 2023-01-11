/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 6/1/2023.
 */

package com.adyen.checkout.card

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

/**
 * Configuration class for Address Form in Address View. This class can be used define the
 * visibility of the address form.
 */
@Parcelize
sealed class AddressParams : Parcelable {

    /**
     * Address Form will be hidden.
     */
    @Parcelize
    object None : AddressParams()

    /**
     * Only postal code will be shown as part of the card component.
     */
    @Parcelize
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
    @Parcelize
    data class FullAddress(
        val defaultCountryCode: String? = null,
        val supportedCountryCodes: List<String> = emptyList(),
        val addressFieldPolicy: AddressFieldPolicy
    ) : AddressParams()
}

/**
 * Configuration for requirement of the address fields.
 */
interface AddressFieldPolicy : Parcelable
