/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.bcmc

import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.base.InputData

data class BcmcInputData(
    var cardNumber: String = "",
    var expiryDate: ExpiryDate = ExpiryDate.EMPTY_DATE,
    var isStorePaymentSelected: Boolean = false,
) : InputData
