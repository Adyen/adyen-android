/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.issuerlist.IssuerListSpinnerView

class OnlineBankingCZSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IssuerListSpinnerView<OnlineBankingCZPaymentMethod, OnlineBankingCZComponent>(context, attrs, defStyleAttr)
