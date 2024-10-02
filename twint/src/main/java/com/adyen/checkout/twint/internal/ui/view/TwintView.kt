/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.twint.R
import com.adyen.checkout.twint.databinding.TwintViewBinding
import com.adyen.checkout.twint.internal.ui.TwintDelegate
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParams
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class TwintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = TwintViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: TwintDelegate

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is TwintDelegate) { "Unsupported delegate type" }
        this.delegate = delegate

        initLocalizedStrings(localizedContext)
        initSwitch()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.switchStorePaymentMethod.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Twint_StorePaymentSwitch,
            localizedContext,
        )
    }

    private fun initSwitch() {
        binding.switchStorePaymentMethod.isVisible =
            (delegate.componentParams as TwintComponentParams).showStorePaymentField
        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            delegate.updateInputData { isStorePaymentSelected = isChecked }
        }
    }

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
