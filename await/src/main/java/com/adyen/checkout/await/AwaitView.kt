/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.await

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.api.ImageLoader.Companion.getInstance
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

class AwaitView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenLinearLayout<AwaitOutputData, AwaitConfiguration, ActionComponentData, AwaitComponent>(
        context,
        attrs,
        defStyleAttr
    ),
    Observer<AwaitOutputData> {

    private lateinit var imageView: ImageView
    private lateinit var textViewOpenApp: TextView
    private lateinit var textViewWaitingConfirmation: TextView
    private lateinit var imageLoader: ImageLoader
    private var paymentMethodType: String? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(getContext()).inflate(R.layout.await_view, this, true)
        val padding = resources.getDimension(R.dimen.standard_double_margin).toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onComponentAttached() {
        imageLoader = getInstance(context, component.configuration.environment)
    }

    override fun initView() {
        imageView = findViewById(R.id.imageView_logo)
        textViewOpenApp = findViewById(R.id.textView_open_app)
        textViewWaitingConfirmation = findViewById(R.id.textView_waiting_confirmation)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        textViewWaitingConfirmation.setLocalizedTextFromStyle(R.style.AdyenCheckout_Await_WaitingConfirmationTextView)
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(awaitOutputData: AwaitOutputData?) {
        Logger.d(TAG, "onChanged")

        if (awaitOutputData == null) {
            return
        }
        if (paymentMethodType == null || paymentMethodType != awaitOutputData.paymentMethodType) {
            paymentMethodType = awaitOutputData.paymentMethodType
            updateMessageText()
            updateLogo()
        }
    }

    override val isConfirmationRequired = false

    override fun highlightValidationErrors() {
        // No validation required
    }

    private fun updateLogo() {
        Logger.d(TAG, "updateLogo - $paymentMethodType")
        paymentMethodType?.let {
            imageLoader.load(it, imageView)
        }
    }

    private fun updateMessageText() {
        messageTextResource?.let {
            textViewOpenApp.text = localizedContext.getString(it)
        }
    }

    @get:StringRes
    private val messageTextResource: Int?
        get() = when (paymentMethodType) {
            PaymentMethodTypes.BLIK -> R.string.checkout_await_message_blik
            PaymentMethodTypes.MB_WAY -> R.string.checkout_await_message_mbway
            else -> null
        }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
