/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.model.payments.request.OnlineBankingSKPaymentMethod
import com.adyen.checkout.onlinebankingcore.OnlineBankingView

class OnlineBankingSKView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : OnlineBankingView<OnlineBankingSKPaymentMethod>(context, attrs, defStyleAttr)
