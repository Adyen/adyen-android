/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/1/2023.
 */

package com.adyen.checkout.econtext

import android.os.Parcel
import com.adyen.checkout.components.core.paymentmethod.EContextPaymentMethod

internal class TestEContextPaymentMethod(
    override var firstName: String? = "firstName",
    @Deprecated("This property is deprecated.")
    override var checkoutAttemptId: String? = "checkoutAttemptId",
    override var lastName: String? = "lastName",
    override var telephoneNumber: String? = "telephoneNumber",
    override var shopperEmail: String? = "shopperEmail",
    override var type: String? = "type"
) : EContextPaymentMethod() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // no need for implementation
    }
}
