/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 3/5/2019.
 */

package com.adyen.checkout.dropin.activity

import android.graphics.drawable.Drawable
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod

class PaymentMethodModel(val paymentMethod: PaymentMethod, logoParam: Drawable?) {

    var isUpdated = false

    var logo: Drawable? = logoParam
        set(value) {
        isUpdated = true
        field = value
    }

    fun consumeUpdate() {
        isUpdated = false
    }
}
