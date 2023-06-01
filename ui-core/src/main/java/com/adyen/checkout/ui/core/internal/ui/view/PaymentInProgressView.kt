/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/2/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RestrictTo
import androidx.constraintlayout.widget.ConstraintLayout
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.exception.CancellationException
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.databinding.ViewPaymentInProgressBinding
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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
    ComponentView {

    private val binding = ViewPaymentInProgressBinding.inflate(LayoutInflater.from(context), this)

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is ActionDelegate) { "Unsupported delegate type" }

        initLocalizedStrings(localizedContext)

        binding.buttonPaymentInProgressCancel.setOnClickListener {
            delegate.onError(CancellationException("Payment in progress was cancelled"))
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        with(binding) {
            textViewPaymentInProgressTitle.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_PaymentInProgressView_TitleTextView,
                localizedContext
            )
            textViewPaymentInProgressDescription.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_PaymentInProgressView_DescriptionTextView,
                localizedContext
            )
            buttonPaymentInProgressCancel.setLocalizedTextFromStyle(
                R.style.AdyenCheckout_PaymentInProgressView_CancelButton,
                localizedContext
            )
        }
    }

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
