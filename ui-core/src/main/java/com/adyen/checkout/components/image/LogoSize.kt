/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/12/2022.
 */

package com.adyen.checkout.components.image

/**
 * The logo size.
 */
@Suppress("unused")
enum class LogoSize {
    /**
     * Size for small logos (height: 26dp).
     */
    SMALL,

    /**
     * Size for medium logos (height: 50dp).
     */
    MEDIUM,

    /**
     * Size for large logos (height: 100dp).
     */
    LARGE;

    override fun toString(): String {
        return name.lowercase()
    }
}
