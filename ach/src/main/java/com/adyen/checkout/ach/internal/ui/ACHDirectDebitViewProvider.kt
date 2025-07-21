/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui

import android.content.Context
import com.adyen.checkout.ach.internal.ui.view.ACHDirectDebitView
import com.adyen.checkout.ui.core.old.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object ACHDirectDebitViewProvider : ViewProvider {
    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            ACHDirectDebitComponentViewType -> ACHDirectDebitView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object ACHDirectDebitComponentViewType : AmountButtonComponentViewType {
    override val viewProvider: ViewProvider = ACHDirectDebitViewProvider
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}
