/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/5/2023.
 */

package com.adyen.checkout.example.extensions

import com.adyen.checkout.core.internal.util.LogUtil

// Inlining is needed to make sure the tag has the name of the calling class and not this file
@Suppress("RestrictedApi", "NOTHING_TO_INLINE")
internal inline fun getLogTag(): String {
    return LogUtil.getTag()
}
