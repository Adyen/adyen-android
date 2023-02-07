/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/1/2023.
 */

package com.adyen.checkout.ach

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration class for Address Form in ACH Component. This class can be used define the
 * visibility of the address form.
 */
sealed class AddressConfiguration : Parcelable {
    /**
     * Address Form will be hidden.
     */
    @Parcelize
    object None : AddressConfiguration()

    /**
     * Full Address Form will be shown as part of the ach component.
     * @param supportedCountryCodes Supported country codes to be filtered from the available country
     * options.
     */
    @Parcelize
    data class FullAddress(
        val supportedCountryCodes: List<String>,
    ) : AddressConfiguration()
}
