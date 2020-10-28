/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/3/2019.
 */

package com.adyen.checkout.example

import androidx.multidex.MultiDexApplication
import android.util.Log
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.example.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CheckoutExampleApplication : MultiDexApplication() {

    companion object {
        init {
            Logger.setLogcatLevel(Log.DEBUG)
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CheckoutExampleApplication)
            modules(appModule)
        }
    }
}
