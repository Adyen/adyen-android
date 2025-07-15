/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import android.content.Context
import com.adyen.checkout.payto.internal.ui.view.PayToView
import com.adyen.checkout.ui.core.old.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object PayToViewProvider : ViewProvider {

    override fun getView(viewType: ComponentViewType, context: Context): ComponentView = when (viewType) {
        PayToComponentViewType -> PayToView(context)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object PayToComponentViewType : AmountButtonComponentViewType {
    override val viewProvider: ViewProvider = PayToViewProvider
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}
