/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.paybybank.databinding.PayByBankViewBinding
import kotlinx.coroutines.CoroutineScope

private val TAG = LogUtil.getTag()

class PayByBankView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: PayByBankViewBinding = PayByBankViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: PayByBankDelegate

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is PayByBankDelegate) throw IllegalArgumentException("Unsupported delegate type")
        this.delegate = delegate
        // TODO init ui
    }

    override val isConfirmationRequired: Boolean = true

    override fun highlightValidationErrors() {
        // TODO highlight
    }

    override fun getView(): View = this
}
