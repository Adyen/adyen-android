/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/6/2026.
 */

package com.adyen.checkout.core.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the result code of a checkout operation.
 *
 * This is a type-safe wrapper around the result code string returned by the Adyen backend. Well-known values are
 * provided as constants in the [companion object][CheckoutResultCode.Companion].
 *
 * For more information about result codes and their meanings, see the
 * [Adyen documentation](https://docs.adyen.com/online-payments/build-your-integration/payment-result-codes).
 *
 * @property value The raw result code string as returned by the backend.
 */
@Parcelize
@JvmInline
value class CheckoutResultCode(val value: String) : Parcelable {
    companion object {
        val AuthenticationNotRequired = CheckoutResultCode("AuthenticationNotRequired")
        val AuthenticationFinished = CheckoutResultCode("AuthenticationFinished")
        val ChallengeShopper = CheckoutResultCode("ChallengeShopper")
        val IdentifyShopper = CheckoutResultCode("IdentifyShopper")
        val RedirectShopper = CheckoutResultCode("RedirectShopper")
        val Received = CheckoutResultCode("Received")
        val Pending = CheckoutResultCode("Pending")
        val PresentToShopper = CheckoutResultCode("PresentToShopper")
        val PartiallyAuthorised = CheckoutResultCode("PartiallyAuthorised")
        val Authorised = CheckoutResultCode("Authorised")
        val Cancelled = CheckoutResultCode("Cancelled")
        val Error = CheckoutResultCode("Error")
        val Refused = CheckoutResultCode("Refused")
    }
}
