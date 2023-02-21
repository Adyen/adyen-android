/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/7/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.util.CountryInfo
import com.adyen.checkout.mbway.internal.ui.model.MBWayInputData
import com.adyen.checkout.mbway.internal.ui.model.MBWayOutputData
import kotlinx.coroutines.flow.Flow

internal interface MBWayDelegate :
    PaymentComponentDelegate<PaymentComponentState<MBWayPaymentMethod>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: MBWayOutputData

    val outputDataFlow: Flow<MBWayOutputData>

    val componentStateFlow: Flow<PaymentComponentState<MBWayPaymentMethod>>

    fun getSupportedCountries(): List<CountryInfo>

    fun updateInputData(update: MBWayInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
