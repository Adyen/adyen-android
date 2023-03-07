/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action.internal.ui

import com.adyen.authentication.AuthenticationLauncher
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.threeds2.customization.UiCustomization

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface GenericActionDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    IntentHandlingDelegate,
    ViewProvidingDelegate {

    val delegate: ActionDelegate

    fun set3DS2UICustomization(uiCustomization: UiCustomization?)

    fun initDelegatedAuthentication(authenticationLauncher: AuthenticationLauncher)

    fun refreshStatus()
}
