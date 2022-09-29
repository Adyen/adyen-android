/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/9/2022.
 */

package com.adyen.checkout.voucher

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.ui.ComponentViewNew
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType

internal object VoucherViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentViewNew {
        return when (viewType) {
            VoucherComponentViewType -> VoucherViewNew(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object VoucherComponentViewType : ComponentViewType
