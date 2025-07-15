/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/9/2022.
 */

package com.adyen.checkout.blik.internal.ui

import android.content.Context
import com.adyen.checkout.blik.internal.ui.view.BlikView
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
