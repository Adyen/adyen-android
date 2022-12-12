/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/12/2022.
 */

package com.adyen.checkout.test.extensions

import androidx.lifecycle.ViewModel

fun ViewModel.invokeOnCleared() {
    with(javaClass.getDeclaredMethod("onCleared")) {
        isAccessible = true
        invoke(this@invokeOnCleared)
    }
}
