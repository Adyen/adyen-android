/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2024.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import com.adyen.checkout.googlepay.GooglePayButtonTheme
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.adyen.checkout.googlepay.R
import com.adyen.checkout.googlepay.databinding.ViewGooglePayButtonBinding
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.view.PayButton
import com.google.android.gms.wallet.button.ButtonOptions

internal class GooglePayButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PayButton(context, attrs, defStyleAttr) {

    private val binding = ViewGooglePayButtonBinding.inflate(LayoutInflater.from(context), this)

    private val styledButtonType: GooglePayButtonType?
    private val styledButtonTheme: GooglePayButtonTheme?
    private val styledCornerRadius: Int?

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GooglePayButtonView,
            defStyleAttr,
            R.style.AdyenCheckout_GooglePay_Button,
        )
        styledButtonType =
            typedArray.getInt(R.styleable.GooglePayButtonView_adyenGooglePayButtonType, -1).mapStyledButtonType()
        styledButtonTheme =
            typedArray.getInt(R.styleable.GooglePayButtonView_adyenGooglePayButtonTheme, -1).mapStyledButtonTheme()
        styledCornerRadius =
            typedArray.getDimensionPixelSize(R.styleable.GooglePayButtonView_adyenGooglePayButtonCornerRadius, -1)
                .mapStyledCornerRadius()
        typedArray.recycle()
    }

    override fun initialize(delegate: ButtonDelegate) {
        check(delegate is GooglePayDelegate)

        val buttonStyle = delegate.componentParams.googlePayButtonStyling

        val buttonType = buttonStyle?.buttonType ?: styledButtonType
        val buttonTheme = buttonStyle?.buttonTheme ?: styledButtonTheme
        val cornerRadius =
            buttonStyle?.cornerRadius?.let { (it * Resources.getSystem().displayMetrics.density).toInt() }
                ?: styledCornerRadius

        binding.payButton.initialize(
            ButtonOptions.newBuilder().apply {
                if (buttonType != null) {
                    setButtonType(buttonType.value)
                }

                if (buttonTheme != null) {
                    setButtonTheme(buttonTheme.value)
                }

                if (cornerRadius != null) {
                    setCornerRadius(cornerRadius)
                }

                setAllowedPaymentMethods(delegate.getGooglePayButtonParameters().allowedPaymentMethods)
            }.build(),
        )
    }

    override fun setEnabled(enabled: Boolean) {
        binding.payButton.isEnabled = enabled
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.payButton.setOnClickListener(listener)
    }

    override fun setText(text: String?) = Unit

    private fun Int.mapStyledButtonType(): GooglePayButtonType? = when (this) {
        0 -> GooglePayButtonType.BUY
        1 -> GooglePayButtonType.BOOK
        2 -> GooglePayButtonType.CHECKOUT
        3 -> GooglePayButtonType.DONATE
        4 -> GooglePayButtonType.ORDER
        5 -> GooglePayButtonType.PAY
        6 -> GooglePayButtonType.SUBSCRIBE
        7 -> GooglePayButtonType.PLAIN
        else -> null
    }

    private fun Int.mapStyledButtonTheme(): GooglePayButtonTheme? = when (this) {
        0 -> GooglePayButtonTheme.LIGHT
        1 -> GooglePayButtonTheme.DARK
        else -> null
    }

    private fun Int.mapStyledCornerRadius(): Int? = if (this == -1) {
        null
    } else {
        this
    }
}
