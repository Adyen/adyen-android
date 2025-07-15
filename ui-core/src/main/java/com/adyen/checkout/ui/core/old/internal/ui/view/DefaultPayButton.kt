/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.adyen.checkout.ui.core.databinding.DefaultPayButtonViewBinding
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import kotlinx.coroutines.CoroutineScope

internal class DefaultPayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PayButton(context, attrs, defStyleAttr) {

    private val binding = DefaultPayButtonViewBinding.inflate(LayoutInflater.from(context), this)

    override fun initialize(delegate: ButtonDelegate, coroutineScope: CoroutineScope) = Unit

    override fun setEnabled(enabled: Boolean) {
        binding.payButton.isEnabled = enabled
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.payButton.setOnClickListener(listener)
    }

    override fun setText(text: String?) {
        binding.payButton.text = text
    }
}
