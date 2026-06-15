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
data class CheckoutResultCode(val value: String) : Parcelable {
    companion object {
        @JvmField
        val AUTHENTICATION_NOT_REQUIRED = CheckoutResultCode("AuthenticationNotRequired")

        @JvmField
        val AUTHENTICATION_FINISHED = CheckoutResultCode("AuthenticationFinished")

        @JvmField
        val CHALLENGE_SHOPPER = CheckoutResultCode("ChallengeShopper")

        @JvmField
        val IDENTIFY_SHOPPER = CheckoutResultCode("IdentifyShopper")

        @JvmField
        val REDIRECT_SHOPPER = CheckoutResultCode("RedirectShopper")

        @JvmField
        val RECEIVED = CheckoutResultCode("Received")

        @JvmField
        val PENDING = CheckoutResultCode("Pending")

        @JvmField
        val PRESENT_TO_SHOPPER = CheckoutResultCode("PresentToShopper")

        @JvmField
        val PARTIALLY_AUTHORISED = CheckoutResultCode("PartiallyAuthorised")

        @JvmField
        val AUTHORISED = CheckoutResultCode("Authorised")

        @JvmField
        val CANCELLED = CheckoutResultCode("Cancelled")

        @JvmField
        val ERROR = CheckoutResultCode("Error")

        @JvmField
        val REFUSED = CheckoutResultCode("Refused")
    }
}
