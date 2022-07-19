/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.googlepay.model.GooglePayParams
import kotlinx.coroutines.flow.Flow

interface GooglePayDelegate : PaymentMethodDelegate<
    GooglePayConfiguration,
    GooglePayInputData,
    GooglePayOutputData,
    GooglePayComponentState> {

    val outputDataFlow: Flow<GooglePayOutputData?>

    val componentStateFlow: Flow<GooglePayComponentState?>

    fun getGooglePayParams(): GooglePayParams
}
