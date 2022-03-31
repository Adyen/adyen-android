/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */
package com.adyen.checkout.googlepay

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.googlepay.util.GooglePayUtils
import com.google.android.gms.wallet.PaymentData

class GooglePayOutputData(val paymentData: PaymentData) : OutputData {

    override val isValid: Boolean = GooglePayUtils.findToken(paymentData).isNotEmpty()
}
