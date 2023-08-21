/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/9/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.mbway.internal.ui.view.MbWayView
import com.adyen.checkout.ui.core.internal.ui.AmountButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object MbWayViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView = when (viewType) {
        MbWayComponentViewType -> MbWayView(context, attrs, defStyleAttr)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object MbWayComponentViewType : AmountButtonComponentViewType {
    override val viewProvider: ViewProvider = MbWayViewProvider
    override val buttonTextResId: Int = ButtonComponentViewType.DEFAULT_BUTTON_TEXT_RES_ID
}