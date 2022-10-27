/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action

import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.threeds2.customization.UiCustomization

interface GenericActionDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    IntentHandlingDelegate,
    ViewProvidingDelegate {

    val delegate: ActionDelegate

    fun set3DS2UICustomization(uiCustomization: UiCustomization?)

    fun refreshStatus()
}
