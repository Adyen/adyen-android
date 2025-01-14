/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/7/2022.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.mbway.MBWayComponentState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.model.MBWayOutputData
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import kotlinx.coroutines.flow.Flow

internal interface MBWayDelegate :
    PaymentComponentDelegate<MBWayComponentState>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: MBWayOutputData

    val outputDataFlow: Flow<MBWayOutputData>

    val componentStateFlow: Flow<MBWayComponentState>

    fun getSupportedCountries(): List<CountryModel>

    fun getInitiallySelectedCountry(): CountryModel?

    fun onFieldValueChanged(fieldId: MBWayFieldId, newValue: String)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
