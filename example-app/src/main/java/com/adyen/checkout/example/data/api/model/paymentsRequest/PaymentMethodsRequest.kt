/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.api.model.paymentsRequest

import com.adyen.checkout.components.model.payments.Amount

@Suppress("MagicNumber")
data class PaymentMethodsRequest(
    val merchantAccount: String,
    val shopperReference: String,
//    val additionalData: Any,
//    val allowedPaymentMethods: ArrayList<String>,
    val amount: Amount,
//    val blockedPaymentMethods: ArrayList<String>,
    val countryCode: String = "NL",
    val shopperLocale: String = "en_US",
    val channel: String = "android",
    val telephoneNumber: String = "0612345678",
    val dateOfBirth: String = "1990-01-01",
    val shopperEmail: String = "shopper@mystoredemo.io",
    val shopperName: ShopperName = ShopperName(),
    val billingAddress: Address = Address(),
    val deliveryAddress: Address = Address(),
    val splitCardFundingSources: Boolean = false
)

@SuppressWarnings("MemberName")
data class ShopperName(val firstName: String = "Jan", val lastName: String = "Jansen", val gender: String = "MALE")

@SuppressWarnings("MemberName")
data class Address(
    val country: String = "NL",
    val city: String = "Capital",
    val houseNumberOrName: String = "1",
    val postalCode: String = "1012 DJ",
    val stateOrProvince: String = "DC",
    val street: String = "Main St"
)
