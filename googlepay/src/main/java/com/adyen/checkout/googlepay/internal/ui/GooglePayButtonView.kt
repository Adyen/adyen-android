/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2024.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.adyen.checkout.googlepay.databinding.ViewGooglePayButtonBinding
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.view.PayButton
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType
import com.google.android.gms.wallet.button.ButtonOptions

internal class GooglePayButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PayButton(context, attrs, defStyleAttr) {

    private val binding = ViewGooglePayButtonBinding.inflate(LayoutInflater.from(context), this)

    override fun initialize(delegate: ButtonDelegate) {
        check(delegate is GooglePayDelegate)

        binding.payButton.initialize(
            ButtonOptions.newBuilder()
                .setButtonType(ButtonType.PAY)
                .setAllowedPaymentMethods(delegate.getGooglePayButtonParameters().allowedPaymentMethods)
                .build(),
        )
    }

    override fun setEnabled(enabled: Boolean) {
        binding.payButton.isEnabled = enabled
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.payButton.setOnClickListener(listener)
    }

    override fun setText(text: String?) = Unit
}
