/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/9/2022.
 */

package com.adyen.checkout.bacs

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ComponentViewType

internal object BacsViewProvider : ViewProvider {
    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView {
        return when (viewType) {
            BacsComponentViewType.INPUT -> BacsDirectDebitInputView(context, attrs, defStyleAttr)
            BacsComponentViewType.CONFIRMATION -> BacsDirectDebitConfirmationView(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal enum class BacsComponentViewType : ComponentViewType {
    INPUT, CONFIRMATION
}
