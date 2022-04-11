/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.api

import androidx.annotation.CallSuper
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * A cancellable [FutureTask] that wraps a [Callable].
 *
 * @param <T> The type returned by the [Callable]
 * @param callable The block of code to be ran.
 * @param timeOut A time out in milliseconds to cancel the connection.
 */
abstract class TimeoutTask<T> protected constructor(
    callable: Callable<T>,
    private val timeOut: Long = 0,
) : FutureTask<T>(callable) {

    @CallSuper
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        Logger.d(TAG, "cancel - $mayInterruptIfRunning")
        return super.cancel(mayInterruptIfRunning)
    }

    override fun run() {
        if (timeOut > 0) {
            Logger.d(TAG, "run with timeout - $timeOut")
        }
        super.run()
        if (timeOut > 0) {
            try {
                get(timeOut, TimeUnit.MILLISECONDS)
            } catch (e: ExecutionException) {
                Logger.d(TAG, "ExecutionException", e)
            } catch (e: InterruptedException) {
                Logger.d(TAG, "InterruptedException", e)
            } catch (e: TimeoutException) {
                Logger.e(TAG, "Task timed out after $timeOut milliseconds.")
                cancel(true)
            }
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
