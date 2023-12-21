/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/12/2023.
 */

package com.adyen.checkout.voucher.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.voucher.R
import kotlinx.coroutines.CoroutineScope

// TODO: After removing BoletoVoucherView, make sure to make FullVoucherView non open, binding to private and remove Bolet styles.
class BoletoVoucherView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FullVoucherView(context, attrs, defStyleAttr) {
    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        super.initView(delegate, coroutineScope, localizedContext)

        binding.textViewIntroduction.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Voucher_Description_Boleto,
            localizedContext
        )
    }
}
