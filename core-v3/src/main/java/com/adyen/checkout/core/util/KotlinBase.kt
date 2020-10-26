/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/9/2020.
 */

package com.adyen.checkout.core.util

import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

// TODO: 15/09/2020 remove after we have some Kotlin code in this module
object KotlinBase {
    private val tag = LogUtil.getTag()

    @JvmStatic
    fun log() {
        Logger.v(tag, "Running Kotlin")
    }
}
