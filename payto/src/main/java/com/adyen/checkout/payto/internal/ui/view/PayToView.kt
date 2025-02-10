/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.payto.R
import com.adyen.checkout.payto.databinding.PaytoViewBinding
import com.adyen.checkout.payto.internal.ui.PayToDelegate
import com.adyen.checkout.payto.internal.ui.model.PayToMode
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class PayToView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = PaytoViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: PayToDelegate

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is PayToDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initModeSelector()
    }

    private fun initModeSelector() {
        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_payId -> togglePayIdViews(isChecked)
                R.id.button_bsb -> toggleBsbViews(isChecked)
            }
        }
        binding.toggleButtonChoice.check(R.id.button_payId)
    }

    private fun togglePayIdViews(isChecked: Boolean) {
        binding.textViewPayIdDescription.isVisible = isChecked
        binding.textViewBsbDescription.isVisible = !isChecked

        if (isChecked) {
            delegate.updateInputData { mode = PayToMode.PAY_ID }
        }
    }

    private fun toggleBsbViews(isChecked: Boolean) {
        binding.textViewPayIdDescription.isVisible = !isChecked
        binding.textViewBsbDescription.isVisible = isChecked

        if (isChecked) {
            delegate.updateInputData { mode = PayToMode.BSB }
        }
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        // TODO Do validation
    }

    override fun getView(): View = this
}
