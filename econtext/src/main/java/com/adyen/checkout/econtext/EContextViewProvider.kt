/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/1/2023.
 */

package com.adyen.checkout.econtext

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType

internal object EContextViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView {
        return when (viewType) {
            EContextComponentViewType -> EContextView(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object EContextComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = EContextViewProvider
}
