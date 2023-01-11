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
import androidx.annotation.RestrictTo
import androidx.constraintlayout.widget.ConstraintLayout
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.ui.databinding.ViewPaymentInProgressBinding
import com.adyen.checkout.core.exception.CancellationException
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
        if (delegate !is ActionDelegate) throw IllegalStateException("Unsupported delegate type")

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
