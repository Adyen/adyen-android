/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */
package com.adyen.checkout.googlepay.util

@Suppress("MemberVisibilityCanBePrivate")
object AllowedCardNetworks {

    const val AMEX = "AMEX"
    const val DISCOVER = "DISCOVER"
    const val INTERAC = "INTERAC"
    const val JCB = "JCB"
    const val MASTERCARD = "MASTERCARD"
    const val VISA = "VISA"

    /**
     * A list of the allowed credit card networks accepted on Google Pay.
     *
     * @return The list of all allowed card networks.
     */
    val allAllowedCardNetworks: List<String> = listOf(AMEX, DISCOVER, INTERAC, JCB, MASTERCARD, VISA)
}
