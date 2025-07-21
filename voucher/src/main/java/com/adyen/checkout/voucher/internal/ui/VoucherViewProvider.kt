/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/9/2022.
 */

package com.adyen.checkout.voucher.internal.ui

import android.content.Context
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvider
import com.adyen.checkout.voucher.internal.ui.VoucherComponentViewType.FULL_VOUCHER
import com.adyen.checkout.voucher.internal.ui.VoucherComponentViewType.SIMPLE_VOUCHER
import com.adyen.checkout.voucher.internal.ui.view.FullVoucherView
import com.adyen.checkout.voucher.internal.ui.view.SimpleVoucherView

internal object VoucherViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
    ): ComponentView {
        return when (viewType) {
            SIMPLE_VOUCHER -> SimpleVoucherView(context)
            FULL_VOUCHER -> FullVoucherView(context)
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }
}

internal enum class VoucherComponentViewType : ComponentViewType {
    SIMPLE_VOUCHER,
    FULL_VOUCHER;

    override val viewProvider: ViewProvider = VoucherViewProvider
}
