/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/12/2022.
 */

package com.adyen.checkout.test.extensions

import androidx.lifecycle.ViewModel

/**
 * Invokes the [ViewModel.onCleared] method. This method is protected, so we can only call it with reflection.
 *
 * Should only be used in tests.
// */
fun ViewModel.invokeOnCleared() {
    var clazz: Class<*> = javaClass
    while (clazz.declaredMethods.toList().none { it.name == "onCleared" }) {
        clazz = clazz.superclass
    }
    with(clazz.getDeclaredMethod("onCleared")) {
        isAccessible = true
        invoke(this@invokeOnCleared)
    }
}
