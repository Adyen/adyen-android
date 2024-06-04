/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.adyen.checkout.cashapppay.databinding.CashAppPayButtonViewBinding
import com.adyen.checkout.ui.core.internal.ui.view.PayButton

internal class CashAppPayButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PayButton(context, attrs, defStyleAttr) {

    private val binding = CashAppPayButtonViewBinding.inflate(LayoutInflater.from(context), this)

    override fun setEnabled(enabled: Boolean) {
        binding.payButton.isEnabled = enabled
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.payButton.setOnClickListener(listener)
    }

    override fun setText(text: String?) = Unit
}
