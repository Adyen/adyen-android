/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/7/2022.
 */

package com.adyen.checkout.mbway

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.components.util.CountryInfo
import kotlinx.coroutines.flow.Flow

interface MBWayDelegate :
    PaymentMethodDelegate,
    ViewProvidingDelegate {

    val outputData: MBWayOutputData

    val outputDataFlow: Flow<MBWayOutputData>

    val componentStateFlow: Flow<PaymentComponentState<MBWayPaymentMethod>>

    fun getSupportedCountries(): List<CountryInfo>

    fun updateInputData(update: MBWayInputData.() -> Unit)
}
