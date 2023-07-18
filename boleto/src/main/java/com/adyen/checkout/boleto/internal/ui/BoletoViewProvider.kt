/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.boleto.R
import com.adyen.checkout.boleto.internal.ui.view.BoletoView
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object BoletoViewProvider : ViewProvider {
    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView = when (viewType) {
        BoletoComponentViewType -> BoletoView(context, attrs, defStyleAttr)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object BoletoComponentViewType : ButtonComponentViewType {

    override val viewProvider: ViewProvider = BoletoViewProvider

    override val buttonTextResId: Int = R.string.checkout_boleto_generate_btn_label
}
