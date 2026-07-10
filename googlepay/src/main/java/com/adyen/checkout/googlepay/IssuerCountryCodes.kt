/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2026.
 */

package com.adyen.checkout.googlepay

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Restricts the card issuers offered through Google Pay by country.
 *
 * See the
 * [CardParameters](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
 * object from the Google Pay SDK for more details.
 */
abstract class IssuerCountryCodes internal constructor() : Parcelable {

    /**
     * Only issuers from the given [codes] are supported.
     *
     * @param codes The ISO 3166-1 alpha-2 country codes of the issuers you support.
     */
    @Parcelize
    data class Allowed(val codes: List<String>) : IssuerCountryCodes()

    /**
     * Issuers from the given [codes] are not supported.
     *
     * @param codes The ISO 3166-1 alpha-2 country codes of the issuers you don't support.
     */
    @Parcelize
    data class Blocked(val codes: List<String>) : IssuerCountryCodes()
}
