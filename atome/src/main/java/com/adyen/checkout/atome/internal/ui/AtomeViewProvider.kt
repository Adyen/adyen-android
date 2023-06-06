/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/6/2023.
 */

package com.adyen.checkout.atome.internal.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.atome.internal.ui.view.AtomeView
import com.adyen.checkout.ui.core.internal.ui.ButtonComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider

internal object AtomeViewProvider : ViewProvider {
    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView = when (viewType) {
        AtomeComponentViewType -> AtomeView(context, attrs, defStyleAttr)
        else -> throw IllegalArgumentException("Unsupported view type")
    }
}

internal object AtomeComponentViewType : ButtonComponentViewType {
    override val buttonTextResId: Int
        get() = TODO("Not yet implemented")
    override val viewProvider: ViewProvider = AtomeViewProvider
}
