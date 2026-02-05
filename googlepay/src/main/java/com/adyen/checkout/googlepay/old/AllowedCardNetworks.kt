/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/12/2023.
 */
package com.adyen.checkout.googlepay.old

/**
 * The card networks accepted by Google Pay.
 */
@Suppress("MemberVisibilityCanBePrivate")
object AllowedCardNetworks {

    const val AMEX = "AMEX"
    const val DISCOVER = "DISCOVER"
    const val INTERAC = "INTERAC"
    const val JCB = "JCB"
    const val MASTERCARD = "MASTERCARD"
    const val VISA = "VISA"

    internal val allAllowedCardNetworks: List<String> = listOf(AMEX, DISCOVER, INTERAC, JCB, MASTERCARD, VISA)
}
