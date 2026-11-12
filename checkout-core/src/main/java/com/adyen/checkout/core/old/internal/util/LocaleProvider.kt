/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/2/2024.
 */

package com.adyen.checkout.core.old.internal.util

import android.content.Context
import androidx.annotation.RestrictTo
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class LocaleProvider {

    fun getLocale(context: Context): Locale {
        return context.resources.configuration.locales[0]
    }
}
