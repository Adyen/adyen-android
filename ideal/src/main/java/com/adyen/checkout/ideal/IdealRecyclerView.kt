/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/5/2019.
 */
package com.adyen.checkout.ideal

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.model.payments.request.IdealPaymentMethod
import com.adyen.checkout.issuerlist.IssuerListRecyclerView

class IdealRecyclerView : IssuerListRecyclerView<IdealPaymentMethod, IdealComponent> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
