/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.base.InputData

data class CardInputData(
    var cardNumber: String = "",
    var expiryDate: ExpiryDate = ExpiryDate.EMPTY_DATE,
    var securityCode: String = "",
    var holderName: String = "",
    var socialSecurityNumber: String = "",
    var kcpBirthDateOrTaxNumber: String = "",
    var kcpCardPassword: String = "",
    var postalCode: String = "",
    var address: AddressInputModel = AddressInputModel(),
    var isStorePaymentSelected: Boolean = false,
    var selectedCardIndex: Int = 0,
    var installmentOption: InstallmentModel? = null
) : InputData
