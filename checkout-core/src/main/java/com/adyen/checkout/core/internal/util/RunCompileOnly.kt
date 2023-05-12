/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/5/2023.
 */

package com.adyen.checkout.core.internal.util

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <R : Any> runCompileOnly(block: () -> R): R? {
    try {
        return block()
    } catch (e: ClassNotFoundException) {
        Logger.w(LogUtil.getTag(), "Class not found. Are you missing a dependency?", e)
    } catch (e: NoClassDefFoundError) {
        Logger.w(LogUtil.getTag(), "Class not found. Are you missing a dependency?", e)
    }

    return null
}
