/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/7/2025.
 */

package com.adyen.checkout.core.common

import android.util.Log

/**
 * The severity levels used by the Adyen logger, ordered from most to least verbose.
 *
 * Use these values with [AdyenLogger.setLogLevel] to control the minimum level that gets logged.
 * [NONE] disables logging entirely.
 *
 * @param priority The underlying Android [Log] priority for this level.
 */
enum class AdyenLogLevel(
    val priority: Int,
) {
    VERBOSE(Log.VERBOSE),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARN(Log.WARN),
    ERROR(Log.ERROR),
    ASSERT(Log.ASSERT),

    @Suppress("MagicNumber")
    NONE(100),
}
