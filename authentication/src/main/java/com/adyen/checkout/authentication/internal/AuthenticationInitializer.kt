/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.authentication.internal

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.adyen.checkout.authentication.internal.ui.AuthenticationFactory
import com.adyen.checkout.core.action.data.ActionTypes
import com.adyen.checkout.core.action.internal.ActionComponentProvider

internal class AuthenticationInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ActionComponentProvider.register(
            actionType = ActionTypes.THREEDS2,
            factory = AuthenticationFactory(context.applicationContext as Application),
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
