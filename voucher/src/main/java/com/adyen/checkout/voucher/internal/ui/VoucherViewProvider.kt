/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/9/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.voucher.internal.ui.view.VoucherView

internal object VoucherViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView {
        return when (viewType) {
            VoucherComponentViewType -> VoucherView(context, attrs, defStyleAttr)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal object VoucherComponentViewType : ComponentViewType {
    override val viewProvider: ViewProvider = VoucherViewProvider
}
