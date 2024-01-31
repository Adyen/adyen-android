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
import com.adyen.checkout.mbway.internal.ui.model.FocussedView
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
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

    val viewStateFlow: Flow<MBWayViewState>

    val componentStateFlow: Flow<MBWayComponentState>

    fun onCountrySelected(country: CountryModel)

    fun onPhoneNumberChanged(phoneNumber: String)

    fun onViewFocussed(focussedView: FocussedView)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)

    fun highlightValidationErrors()
}
