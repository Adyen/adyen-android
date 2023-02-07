/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2023.
 */

package com.adyen.checkout.upi

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.isVisible
import com.adyen.checkout.components.ui.ComponentView
import com.adyen.checkout.upi.databinding.UpiViewBinding
import kotlinx.coroutines.CoroutineScope

internal class UpiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = UpiViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.button_vpa -> {
                    binding.textInputLayoutVpa.isVisible = true
                    binding.textViewQrCodeDescription.isVisible = false
                }
                R.id.button_qrCode -> {
                    binding.textInputLayoutVpa.isVisible = false
                    binding.textViewQrCodeDescription.isVisible = true
                }
            }
        }
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        // TODO
    }

    override fun highlightValidationErrors() {
        // TODO
    }

    override fun getView(): View = this
}
