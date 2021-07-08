/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/5/2021.
 */

package com.adyen.checkout.components.base

import android.app.Activity
import android.content.Intent

/**
 * A component that expects to receive and handle an external result in the form of an [Intent].
 */
interface IntentHandlingComponent : ResultHandlingComponent {

    /**
     * Handles the [Intent] corresponding to this component, if valid.
     * Depending on your implementation, extract the [Intent] and pass it to this method.
     * In most cases it can be retrieved with either [Activity.getIntent] or [Activity.onNewIntent].
     *
     * @param intent the received [Intent].
     */
    fun handleIntent(intent: Intent)
}
