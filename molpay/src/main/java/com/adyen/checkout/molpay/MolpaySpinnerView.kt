/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */
package com.adyen.checkout.molpay

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.issuerlist.IssuerListSpinnerView

class MolpaySpinnerView : IssuerListSpinnerView<MolpayPaymentMethod, MolpayComponent> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
