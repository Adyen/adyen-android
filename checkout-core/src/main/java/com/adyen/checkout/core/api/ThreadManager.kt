/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.api

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ThreadManager {

    @Suppress("TooGenericExceptionCaught")
    @JvmField
    val MAIN_HANDLER = try {
        Handler(Looper.getMainLooper())
    } catch (e: RuntimeException) {
        // avoid Looper class on testing
        Handler()
    }
    @JvmField
    val EXECUTOR: ExecutorService = Executors.newCachedThreadPool()
}
