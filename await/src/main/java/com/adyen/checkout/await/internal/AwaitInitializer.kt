/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.await.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.adyen.checkout.await.internal.ui.AwaitFactory
import com.adyen.checkout.core.action.data.ActionTypes
import com.adyen.checkout.core.action.internal.ActionComponentProvider

@Keep
internal class AwaitInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ActionComponentProvider.register(ActionTypes.AWAIT, AwaitFactory())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
