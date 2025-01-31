/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2022.
 */
package com.adyen.checkout.await.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.adyen.checkout.await.R
import com.adyen.checkout.await.databinding.AwaitViewBinding
import com.adyen.checkout.await.internal.ui.AwaitDelegate
import com.adyen.checkout.await.internal.ui.model.AwaitOutputData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

internal class AwaitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr,
    ),
    ComponentView {

    private val binding: AwaitViewBinding = AwaitViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: AwaitDelegate

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is AwaitDelegate) { "Unsupported delegate type" }

        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewWaitingConfirmation.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Await_WaitingConfirmationTextView,
            localizedContext,
        )
    }

    private fun observeDelegate(delegate: AwaitDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(outputData: AwaitOutputData) {
        adyenLog(AdyenLogLevel.DEBUG) { "outputDataChanged" }

        updateMessageText(outputData.paymentMethodType)
        updateLogo(outputData.paymentMethodType)
    }

    override fun highlightValidationErrors() {
        // No validation required
    }

    private fun updateMessageText(paymentMethodType: String?) {
        getMessageTextResource(paymentMethodType)?.let {
            binding.textViewOpenApp.text = localizedContext.getString(it)
        }
    }

    private fun updateLogo(paymentMethodType: String?) {
        adyenLog(AdyenLogLevel.DEBUG) { "updateLogo - $paymentMethodType" }
        paymentMethodType?.let { txVariant ->
            binding.imageViewLogo.loadLogo(
                environment = delegate.componentParams.environment,
                txVariant = txVariant,
            )
        }
    }

    @StringRes
    private fun getMessageTextResource(paymentMethodType: String?): Int? {
        return when (paymentMethodType) {
            PaymentMethodTypes.BLIK -> R.string.checkout_await_message_blik
            PaymentMethodTypes.MB_WAY -> R.string.checkout_await_message_mbway
            // TODO Change PAY_TO text
            PaymentMethodTypes.PAY_TO -> R.string.checkout_await_message_mbway
            PaymentMethodTypes.UPI_COLLECT -> R.string.checkout_await_message_upi
            else -> null
        }
    }

    override fun getView(): View = this
}
