/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/6/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.internal.ui.view.DefaultPayButton
import com.adyen.checkout.ui.core.internal.ui.view.PayButton

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ButtonViewProvider {
    fun getButton(
        context: Context,
    ): PayButton
}

internal class DefaultButtonViewProvider : ButtonViewProvider {

    override fun getButton(context: Context): PayButton = DefaultPayButton(context)
}
