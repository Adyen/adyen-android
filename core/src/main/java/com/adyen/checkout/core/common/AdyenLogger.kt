/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.common

import com.adyen.checkout.core.common.internal.helper.AdyenLogDispatcher

/**
 * Configures the logging of the Adyen SDK. Logs are written to Logcat.
 */
object AdyenLogger {

    /**
     * Sets the minimum [LogLevel] to be logged. Messages below this level are ignored.
     *
     * The default is [LogLevel.OFF], so nothing is logged unless a level is set.
     *
     * [LogLevel.DEBUG] and [LogLevel.VERBOSE] can include sensitive data: [LogLevel.DEBUG] logs redirect URLs and
     * redirect results, and [LogLevel.VERBOSE] also logs request and response bodies and response headers. Only use
     * these levels during development.
     *
     * @param level The minimum level to be logged.
     */
    @JvmStatic
    fun setLogLevel(level: LogLevel) {
        AdyenLogDispatcher.logLevel = level
    }

    /**
     * The severity levels used by the Adyen logger, ordered from most to least verbose.
     *
     * [OFF] disables logging entirely.
     */
    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        ASSERT,
        OFF,
    }
}

typealias AdyenLogLevel = AdyenLogger.LogLevel
