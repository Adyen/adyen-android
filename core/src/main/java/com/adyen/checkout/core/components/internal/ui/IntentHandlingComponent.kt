/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/10/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import android.content.Intent
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface IntentHandlingComponent {

    /**
     * Handles the [android.content.Intent] corresponding to this component, if valid.
     * Depending on your implementation, extract the [android.content.Intent] and pass it to this method.
     * In most cases it can be retrieved with either [android.app.Activity.getIntent] or
     * [android.app.Activity.onNewIntent].
     *
     * @param intent the received [android.content.Intent].
     */
    fun handleIntent(intent: Intent)
}
