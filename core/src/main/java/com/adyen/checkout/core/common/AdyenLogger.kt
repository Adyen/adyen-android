/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.common

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.helper.LogcatLogger

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
        fun setLogLevel(level: AdyenLogLevel) {
            didSetLogLevelManually = true
            logger.setLogLevel(level)
        }

        /**
         * Set your own custom instance of [AdyenLogger].
         */
        fun setLogger(logger: AdyenLogger) {
            Companion.logger = logger
        }

        /**
         * Reset the logger instance back to the default.
         */
        fun resetLogger() {
            logger = LogcatLogger()
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
