package com.adyen.checkout.cashapppay.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.adyen.checkout.cashapppay.R
import com.adyen.checkout.cashapppay.databinding.CashAppPayWaitingViewBinding
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

internal class CashAppPayWaitingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = CashAppPayWaitingViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        initLocalizedStrings(localizedContext)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewPaymentInProgressDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_CashAppPay_WaitingDescriptionTextView,
            localizedContext
        )
    }

    override fun highlightValidationErrors() = Unit

    override fun getView(): View = this
}
