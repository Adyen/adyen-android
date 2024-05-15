/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.googlepay.GooglePayButtonParameters
import com.adyen.checkout.googlepay.GooglePayComponentState
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface GooglePayDelegate :
    PaymentComponentDelegate<GooglePayComponentState>,
    ViewProvidingDelegate {

    val componentStateFlow: Flow<GooglePayComponentState>

    val exceptionFlow: Flow<CheckoutException>

    fun startGooglePayScreen(activity: Activity, requestCode: Int)

    fun handleActivityResult(resultCode: Int, data: Intent?)

    fun getGooglePayButtonParameters(): GooglePayButtonParameters
}
