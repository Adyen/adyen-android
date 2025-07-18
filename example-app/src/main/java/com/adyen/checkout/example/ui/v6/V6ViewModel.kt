/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.example.ui.v6

import androidx.lifecycle.ViewModel
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.AdyenCheckout
import com.adyen.checkout.core.components.CheckoutCallback
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.example.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class V6ViewModel @Inject constructor() : ViewModel(), CheckoutCallback {

    private val configuration = CheckoutConfiguration(
        Environment.TEST,
        BuildConfig.CLIENT_KEY,
    )

    fun createAdyenCheckout() = AdyenCheckout(
        checkoutSession = null,
        checkoutConfiguration = configuration,
        checkoutCallback = this,
    )

    override suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
        // TODO - make payments call
        return CheckoutResult.Finished()
    }
}
