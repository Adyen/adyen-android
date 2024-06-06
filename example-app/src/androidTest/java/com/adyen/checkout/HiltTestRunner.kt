/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/4/2024.
 */

package com.adyen.checkout

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.AdyenLogger
import dagger.hilt.android.testing.HiltTestApplication

// This class is used from build.gradle
@Suppress("Unused")
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        AdyenLogger.setLogLevel(AdyenLogLevel.VERBOSE)
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
