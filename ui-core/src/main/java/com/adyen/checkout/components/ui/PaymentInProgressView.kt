/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/9/2022.
 */

package com.adyen.checkout.components.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.ui.databinding.ViewPaymentInProgressBinding
import kotlinx.coroutines.CoroutineScope

class PaymentInProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentViewNew {

    private val binding = ViewPaymentInProgressBinding.inflate(LayoutInflater.from(context), this)

    init {
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        initLocalizedStrings(localizedContext)

        binding.buttonPaymentinprogressAbort.setOnClickListener {
        }
    }

    @Suppress("SetTextI18n", "UNUSED_PARAMETER")
    private fun initLocalizedStrings(localizedContext: Context) {
        with(binding) {
            // TODO: Use string resources when we have final design
            textviewPaymentinprogressDescription.text = "Waiting for confirmation"
            buttonPaymentinprogressAbort.text = "I'm too broke"
        }
    }

    override val isConfirmationRequired: Boolean = false

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
