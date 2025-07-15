/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/10/2022.
 */

package com.adyen.checkout.paybybank.internal.ui

import android.content.Context
import com.adyen.checkout.paybybank.internal.ui.view.PayByBankView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object PayByBankViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            PayByBankComponentViewType -> PayByBankView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object PayByBankComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = PayByBankViewProvider
}
