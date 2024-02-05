/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/5/2023.
 */

package com.adyen.checkout.example.extensions

internal fun Any.getLogTag(): String {
    val fullClassName = this::class.java.simpleName
    val outerClassName = fullClassName.substringBefore('$').substringAfterLast('.')
    return "EX." + if (outerClassName.isEmpty()) {
        fullClassName
    } else {
        outerClassName.removeSuffix("Kt")
    }
}
