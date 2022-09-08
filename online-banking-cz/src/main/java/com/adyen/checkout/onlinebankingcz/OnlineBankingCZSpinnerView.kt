/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.ui.util.ThemeUtil
import com.adyen.checkout.issuerlist.IssuerListSpinnerView

class OnlineBankingCZSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IssuerListSpinnerView<OnlineBankingCZPaymentMethod, OnlineBankingCZComponent>(context, attrs, defStyleAttr) {

    override fun onComponentAttached() {
        super.onComponentAttached()
        termsAndConditionsTextView?.visibility = View.VISIBLE
        termsAndConditionsTextView?.setOnClickListener { launchDownloadIntent() }
    }

    private fun launchDownloadIntent() {
        val url = component.getTermsAndConditionsUrl()
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()
        intent.launchUrl(context, Uri.parse(url))
    }
}
