/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/11/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.threeds2.customization.UiCustomization

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionHandlingComponent {

    fun canHandleAction(action: Action): Boolean

    fun handleAction(action: Action, activity: Activity)

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    fun handleIntent(intent: Intent)

    fun set3DS2UICustomization(uiCustomization: UiCustomization?)
}
