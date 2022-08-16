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
import com.adyen.checkout.core.log.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CheckoutExampleApplication : Application() {

    companion object {
        init {
            Logger.setLogcatLevel(Log.VERBOSE)
        }
    }
}
