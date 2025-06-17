/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/2/2024.
 */

package com.adyen.checkout.core.old

import android.util.Log

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
