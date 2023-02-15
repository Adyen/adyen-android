/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/8/2022.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.threeds2.customization.UiCustomization

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Adyen3DS2Delegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    IntentHandlingDelegate,
    ViewProvidingDelegate {

    fun set3DS2UICustomization(uiCustomization: UiCustomization?)
}
