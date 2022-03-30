/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/7/2019.
 */
package com.adyen.checkout.googlepay.util

object AllowedAuthMethods {

    private const val PAN_ONLY = "PAN_ONLY"
    private const val CRYPTOGRAM_3DS = "CRYPTOGRAM_3DS"

    /**
     * The the Google Pay authentication methods accepted by Adyen.
     *
     * @return A list of the allowed authentication methods.
     */
    val allAllowedAuthMethods: List<String> = listOf(PAN_ONLY, CRYPTOGRAM_3DS)
}
