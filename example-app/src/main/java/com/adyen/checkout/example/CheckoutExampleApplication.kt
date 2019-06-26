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
import com.adyen.checkout.core.code.Lint
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

class CheckoutExampleApplication : Application() {

    companion object {
        @Suppress(Lint.PROTECTED_IN_FINAL)
        protected val TAG = LogUtil.getTag()

        init {
            Logger.setLogcatLevel(Log.DEBUG)
            Logger.d(TAG, "Logger set to DEBUG level.")
        }
    }
}
