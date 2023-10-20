/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.activity.ComponentActivity
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.twint.TwintComponentState
import kotlinx.coroutines.flow.Flow

internal interface TwintDelegate : PaymentComponentDelegate<TwintComponentState> {

    val componentStateFlow: Flow<TwintComponentState>

    val exceptionFlow: Flow<CheckoutException>

    fun startTwintScreen(activity: ComponentActivity)
}
