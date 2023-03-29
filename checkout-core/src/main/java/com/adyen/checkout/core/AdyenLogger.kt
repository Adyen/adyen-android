/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/3/2023.
 */

package com.adyen.checkout.core

import com.adyen.checkout.core.internal.util.Logger

/**
 * Utility class to configure the Adyen logger.
 */
object AdyenLogger {

    /**
     * Sets the minimum level to be logged.
     */
    fun setLogLevel(@Logger.LogLevel logLevel: Int) {
        Logger.setLogLevel(logLevel)
    }
}
