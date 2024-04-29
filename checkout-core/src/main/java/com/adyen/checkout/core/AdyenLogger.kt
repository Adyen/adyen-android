/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/3/2023.
 */

package com.adyen.checkout.core

import android.util.Log
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.util.LogcatLogger
import com.adyen.checkout.core.internal.util.Logger

/**
 * Utility class to configure the Adyen logger.
 */
interface AdyenLogger {

    fun shouldLog(level: AdyenLogLevel): Boolean

    fun setLogLevel(level: AdyenLogLevel)

    fun log(
        level: AdyenLogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    )

    companion object {

        @PublishedApi
        @Volatile
        internal var logger: AdyenLogger = LogcatLogger()
            private set

        private var didSetLogLevelManually = false

        /**
         * Sets the minimum level to be logged.
         */
        @Deprecated(
            "Logger.LogLevel is deprecated.",
            ReplaceWith(
                "AdyenLogger.setLogLevel(AdyenLogLevel.)",
                "com.adyen.checkout.core.AdyenLogLevel",
                "com.adyen.checkout.core.AdyenLogger",
            ),
        )
        fun setLogLevel(@Logger.LogLevel logLevel: Int) {
            val mappedLevel = when (logLevel) {
                Log.VERBOSE -> AdyenLogLevel.VERBOSE
                Log.DEBUG -> AdyenLogLevel.DEBUG
                Log.INFO -> AdyenLogLevel.INFO
                Log.WARN -> AdyenLogLevel.WARN
                Log.ERROR -> AdyenLogLevel.ERROR
                else -> AdyenLogLevel.NONE
            }
            setLogLevel(mappedLevel)
        }

        /**
         * Sets the minimum level to be logged.
         */
        fun setLogLevel(level: AdyenLogLevel) {
            didSetLogLevelManually = true
            logger.setLogLevel(level)
        }

        /**
         * Set your own custom instance of [AdyenLogger].
         */
        fun setLogger(logger: AdyenLogger) {
            this.logger = logger
        }

        /**
         * Reset the logger instance back to the default.
         */
        fun resetLogger() {
            this.logger = LogcatLogger()
            didSetLogLevelManually = false
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun setInitialLogLevel(level: AdyenLogLevel) {
            if (!didSetLogLevelManually) {
                logger.setLogLevel(level)
            }
        }
    }
}
