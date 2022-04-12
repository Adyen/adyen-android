/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/3/2019.
 */
package com.adyen.checkout.components.api

import android.graphics.drawable.BitmapDrawable
import com.adyen.checkout.core.api.ThreadManager
import com.adyen.checkout.core.api.TimeoutTask
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LogoTask(
    private val logoApi: LogoApi,
    private val logoUrl: String,
    callback: LogoCallback
) : TimeoutTask<BitmapDrawable>(
    { LogoService().getLogo(logoUrl) }
) {

    var callbacks: HashSet<LogoCallback> = hashSetOf(callback)

    override fun done() {
        Logger.v(TAG, "done")
        if (isCancelled) {
            Logger.d(TAG, "canceled")
            notifyFailed()
            return
        }
        try {
            // timeout just to make sure we don't get stuck, get call is blocking but should be finished or canceled by now.
            val result = get(SAFETY_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            notifyLogo(result)
        } catch (e: ExecutionException) {
            Logger.e(TAG, "Execution failed for logo  - $logoUrl")
            notifyFailed()
        } catch (e: InterruptedException) {
            Logger.e(TAG, "Execution interrupted.", e)
            notifyFailed()
        } catch (e: TimeoutException) {
            Logger.e(TAG, "Execution timed out.", e)
            notifyFailed()
        }
    }

    fun addCallback(callback: LogoCallback) {
        synchronized(this) {
            callbacks.add(callback)
        }
    }

    private fun notifyLogo(drawable: BitmapDrawable) {
        ThreadManager.MAIN_HANDLER.post {
            logoApi.taskFinished(logoUrl, drawable)
            notifyCallbacksReceived(drawable)
        }
    }

    private fun notifyFailed() {
        ThreadManager.MAIN_HANDLER.post {
            logoApi.taskFinished(logoUrl, null)
            notifyCallbacksFailed()
        }
    }

    private fun notifyCallbacksReceived(drawable: BitmapDrawable) {
        synchronized(this) {
            callbacks.forEach { it.onLogoReceived(drawable) }
            callbacks.clear() // Clearing callbacks to avoid memory leaks.
        }
    }

    private fun notifyCallbacksFailed() {
        synchronized(this) {
            callbacks.forEach { it.onReceiveFailed() }
            callbacks.clear() // Clearing callbacks to avoid memory leaks.
        }
    }

    /**
     * Interface to receive events on logo task completion.
     */
    interface LogoCallback {
        /**
         * This method will be called on the Main Thread when the logo is received.
         *
         * @param drawable The requested logo, or an instance of Placeholder logo if the request failed.
         */
        fun onLogoReceived(drawable: BitmapDrawable)

        /**
         * This method will be called on the Main Thread if there was an error retrieving the logo.
         */
        fun onReceiveFailed()
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val SAFETY_TIMEOUT = 100
    }
}
