/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action.core.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.components.core.internal.ui.PermissionRequestingDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface GenericActionDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    IntentHandlingDelegate,
    ViewProvidingDelegate,
    PermissionRequestingDelegate {

    val delegate: ActionDelegate

    fun refreshStatus()

    fun setOnRedirectListener(listener: () -> Unit)
}
