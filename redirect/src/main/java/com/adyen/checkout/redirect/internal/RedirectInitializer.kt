/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/10/2025.
 */

package com.adyen.checkout.redirect.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.adyen.checkout.core.action.data.ActionTypes
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.redirect.internal.ui.RedirectFactory

@Keep
internal class RedirectInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ActionComponentProvider.register(ActionTypes.REDIRECT, RedirectFactory())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
