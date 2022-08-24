/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */
package com.adyen.checkout.action

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action

class GenericActionComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: GenericActionConfiguration,
) : BaseActionComponent<GenericActionConfiguration>(savedStateHandle, application, configuration),
    IntentHandlingComponent {

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        // TODO
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        // TODO
    }

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<GenericActionComponent, GenericActionConfiguration> =
            GenericActionComponentProvider()
    }
}
