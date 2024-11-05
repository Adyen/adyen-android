/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus.internal

import android.content.Context
import com.adyen.checkout.paybybankus.R
import com.adyen.checkout.paybybankus.internal.ui.view.PayByBankUSView
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object PayByBankUSViewProvider : ViewProvider {
    override fun getView(viewType: ComponentViewType, context: Context): ComponentView {
        return when (viewType) {
            PayByBankUSComponentViewType -> PayByBankUSView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object PayByBankUSComponentViewType : ButtonComponentViewType {
    override val viewProvider: ViewProvider = PayByBankUSViewProvider
    override val buttonTextResId: Int = R.string.checkout_pay_by_bank_us_submit
}
