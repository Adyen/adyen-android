/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.onlinebankingcore.utils

import android.os.Parcel
import com.adyen.checkout.components.core.paymentmethod.IssuerListPaymentMethod

internal class TestOnlineBankingPaymentMethod(
    override var issuer: String? = "issuer",
    @Deprecated("This property is deprecated.")
    override var checkoutAttemptId: String? = "checkoutAttemptId",
    override var sdkData: String? = "sdkData",
    override var type: String? = "type"
) : IssuerListPaymentMethod() {

    override fun writeToParcel(p0: Parcel, p1: Int) {
        // no need for implementation
    }
}
