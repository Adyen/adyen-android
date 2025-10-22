/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/2/2023.
 */

package com.adyen.checkout.sessions.core

import android.os.Parcel
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

internal class TestPaymentMethod(
    override var type: String? = null,
    @Deprecated("This property is deprecated.")
    override var checkoutAttemptId: String? = null,
) : PaymentMethodDetails() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        // noop
    }
}
