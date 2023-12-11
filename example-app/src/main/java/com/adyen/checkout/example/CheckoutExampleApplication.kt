/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/3/2019.
 */

package com.adyen.checkout.example

import android.app.Application
import android.util.Log
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.example.ui.theme.NightThemeRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CheckoutExampleApplication : Application() {

    @Inject
    internal lateinit var nightThemeRepository: NightThemeRepository

    init {
        AdyenLogger.setLogLevel(Log.VERBOSE)
    }

    override fun onCreate() {
        super.onCreate()
        nightThemeRepository.initialize()
    }
}
