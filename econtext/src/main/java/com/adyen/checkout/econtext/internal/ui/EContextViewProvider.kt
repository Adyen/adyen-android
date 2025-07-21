/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/1/2023.
 */

package com.adyen.checkout.econtext.internal.ui

import android.content.Context
import com.adyen.checkout.econtext.internal.ui.view.EContextView
import com.adyen.checkout.ui.core.old.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider

internal object EContextViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            EContextComponentViewType -> EContextView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object EContextComponentViewType : ButtonComponentViewType {
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
    override val viewProvider: ViewProvider = EContextViewProvider
}
