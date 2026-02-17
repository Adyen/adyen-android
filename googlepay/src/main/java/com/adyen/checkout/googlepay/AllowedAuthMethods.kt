/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/12/2023.
 */
package com.adyen.checkout.googlepay

/**
 * The authentication methods accepted by Google Pay.
 */
@Suppress("MemberVisibilityCanBePrivate")
object AllowedAuthMethods {

    const val PAN_ONLY = "PAN_ONLY"
    const val CRYPTOGRAM_3DS = "CRYPTOGRAM_3DS"

    internal val allAllowedAuthMethods: List<String> = listOf(PAN_ONLY, CRYPTOGRAM_3DS)
}
