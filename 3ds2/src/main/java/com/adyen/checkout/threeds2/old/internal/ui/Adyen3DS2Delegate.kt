/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.threeds2.old.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.components.core.internal.ui.RedirectableDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Adyen3DS2Delegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    IntentHandlingDelegate,
    ViewProvidingDelegate,
    RedirectableDelegate
