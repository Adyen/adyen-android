/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/2/2021.
 */

package com.adyen.checkout.components.ui

import androidx.annotation.StringRes

sealed class Validation {

    /**
     * Field is valid and can be accepted.
     */
    object Valid : Validation()
    /**
     * Field is not valid.
     */
    class Invalid(@StringRes val reason: Int) : Validation()

    fun isValid(): Boolean {
        return this is Valid
    }
}
