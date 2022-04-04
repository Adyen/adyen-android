/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/10/2019.
 */

package com.adyen.checkout.example.data

class Scheme(
    val cardNumber: String,
    val cvc: String,
    val expiryDate: String,
    val holderName: String = "",
    val threeds2: Threeds2? = null
)

class Threeds2(val username: String, val password: String)

val normalScheme = Scheme(cardNumber = "4111 1111 1111 1111", cvc = "737", expiryDate = "10/20")
val threeds2Scheme = Scheme(
    cardNumber = "5454 5454 5454 5454",
    cvc = "737",
    expiryDate = "10/20",
    threeds2 = Threeds2("username", "1234")
)

const val IDEAL_WEBVIEW_REDIRECT_KEY = "Continue"
const val RESULT_KEY_AUTHORISED = "Authorised"
const val RESULT_KEY_REFUSED = "Refused"
const val CREDIT_CARD = "Credit Card"
const val IDEAL = "iDEAL"
const val FIRST_STORED_PAYMENT_METHOD = 1
const val FOOTER_OF_PAYEMENT_METHODS_FOOTER = 2
