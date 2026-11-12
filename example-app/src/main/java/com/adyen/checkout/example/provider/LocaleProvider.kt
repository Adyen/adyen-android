/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.provider

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

internal class LocaleProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val locale: Locale by lazy {
        context.resources.configuration.locales[0]
    }
}
