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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.databinding.ProcessingPaymentViewBinding
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ProcessingPaymentView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = ProcessingPaymentViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        initLocalizedStrings(localizedContext)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewPaymentInProgressDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_ProcessingPaymentView_WaitingDescriptionTextView,
            localizedContext,
        )
    }

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
