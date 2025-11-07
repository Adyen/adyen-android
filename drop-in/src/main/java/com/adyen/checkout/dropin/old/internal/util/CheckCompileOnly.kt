/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/12/2023.
 */

package com.adyen.checkout.dropin.old.internal.util

import com.adyen.checkout.core.old.internal.util.runCompileOnly

internal inline fun checkCompileOnly(block: () -> Boolean): Boolean {
    return runCompileOnly(block) ?: false
}
