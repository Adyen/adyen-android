/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/8/2022.
 */

package com.adyen.checkout.adyen3ds2

import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.model.payments.response.BaseThreeds2Action
import com.adyen.threeds2.customization.UiCustomization

interface Adyen3DS2Delegate :
    ActionDelegate<BaseThreeds2Action>,
    DetailsEmittingDelegate,
    IntentHandlingDelegate {

    fun set3DS2UICustomization(uiCustomization: UiCustomization?)
}
