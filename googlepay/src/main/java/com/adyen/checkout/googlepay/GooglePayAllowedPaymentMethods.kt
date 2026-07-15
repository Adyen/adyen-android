/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/7/2026.
 */

package com.adyen.checkout.googlepay

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The payment methods supported by Google Pay. Pass this to [GooglePayConfiguration.allowedPaymentMethods] to
 * configure which payment methods are offered.
 *
 * This maps to the
 * [allowedPaymentMethods](https://developers.google.com/pay/api/android/reference/request-objects#PaymentDataRequest)
 * array of the Google Pay `PaymentDataRequest`. Each type can only be configured once, as Google Pay
 * does not allow the same payment method type to appear more than once. A dedicated field is exposed
 * per supported type to enforce this.
 *
 * When all fields are null a single [Card] is used by default.
 *
 * @param card Parameters for the `CARD` payment method type. See [Card].
 */
@Parcelize
data class GooglePayAllowedPaymentMethods(
    val card: Card? = null,
) : Parcelable {

    /**
     * Parameters for the `CARD` payment method type. This is a mapping of the
     * [CardParameters](https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
     * object from the Google Pay SDK.
     *
     * @param allowedAuthMethods Fields supported to authenticate a card transaction. See
     * [AllowedAuthMethods] for the possible values. Defaults to all supported values when null.
     * @param allowedCardNetworks The card networks you support. See [AllowedCardNetworks] for the
     * possible values. Defaults to the networks returned by the Adyen API when null.
     * @param allowPrepaidCards Set to false if you don't support prepaid cards.
     * @param allowCreditCards Set to false if you don't support credit cards.
     * @param issuerCountryCodes Restricts the supported issuers by country. Leaves the decision to Google Pay
     * when null.
     * @param assuranceDetailsRequired Set to true to request assurance details.
     * @param billingAddressRequired Set to true if you require a billing address.
     * @param billingAddressParameters The billing address parameters. See [BillingAddressParameters].
     */
    @Parcelize
    data class Card(
        val allowedAuthMethods: List<String>? = null,
        val allowedCardNetworks: List<String>? = null,
        val allowPrepaidCards: Boolean? = null,
        val allowCreditCards: Boolean? = null,
        val issuerCountryCodes: IssuerCountryCodes? = null,
        val assuranceDetailsRequired: Boolean? = null,
        val billingAddressRequired: Boolean? = null,
        val billingAddressParameters: BillingAddressParameters? = null,
    ) : Parcelable
}
