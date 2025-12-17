/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.old.internal.ui

import android.content.Context
import com.adyen.checkout.blik.old.internal.ui.view.BlikView
import com.adyen.checkout.ui.core.old.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object BlikViewProvider : ViewProvider {
    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            BlikComponentViewType -> BlikView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object BlikComponentViewType : AmountButtonComponentViewType {
    override val viewProvider: ViewProvider = BlikViewProvider
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}
