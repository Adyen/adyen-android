/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/5/2019.
 */
package com.adyen.checkout.components.models

import android.os.Parcel
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

class TestPaymentMethod(
    override var type: String? = null,
) : PaymentMethodDetails() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        // noop
    }
}
