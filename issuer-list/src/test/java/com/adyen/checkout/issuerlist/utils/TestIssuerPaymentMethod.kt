/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/8/2022.
 */

package com.adyen.checkout.issuerlist.utils

import android.os.Parcel
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod

internal class TestIssuerPaymentMethod(
    override var issuer: String? = "issuer", override var type: String? = "type"
) : IssuerListPaymentMethod() {

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        //mo need for implementation
    }
}
