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
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
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
        orientation = VERTICAL

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)

        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_vpa -> {
                    binding.textInputLayoutVpa.isVisible = isChecked
                    binding.textViewQrCodeDescription.isVisible = !isChecked
                }
                R.id.button_qrCode -> {
                    binding.textInputLayoutVpa.isVisible = !isChecked
                    binding.textViewQrCodeDescription.isVisible = isChecked
                }
            }
        }
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is UpiDelegate) throw IllegalArgumentException("Unsupported delegate type")
    }

    override fun highlightValidationErrors() {
        // TODO
    }

    override fun getView(): View = this
}
