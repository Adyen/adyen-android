/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2023.
 */

package com.adyen.checkout.components.core

interface BinComponentCallback {

    fun onBinValue(): Unit = Unit

    fun onBinLookup(
        type: String,
        brands: List<String>,
    ): Unit = Unit
}
