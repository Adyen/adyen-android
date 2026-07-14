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

    /**
     * Returns whether a message logged at the given [level] should be emitted, based on the
     * currently configured minimum log level.
     *
     * @param level The level of the message to be logged.
     * @return `true` if the message should be logged, `false` otherwise.
     */
    fun shouldLog(level: AdyenLogLevel): Boolean

    /**
     * Sets the minimum [AdyenLogLevel] to be logged. Messages below this level are ignored.
     *
     * @param level The minimum level to be logged.
     */
    fun setLogLevel(level: AdyenLogLevel)

    /**
     * Logs a message with the given [level].
     *
     * @param level The severity level of the message.
     * @param tag The tag identifying the source of the message.
     * @param message The message to be logged.
     * @param throwable An optional [Throwable] to log alongside the message.
     */
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
