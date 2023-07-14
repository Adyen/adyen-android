/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/7/2023.
 */

package com.adyen.checkout.dropin.internal.ui

interface ExtraComponentCallbacks {

    fun onBinLookup(type: String, brands: List<String>) = Unit

    fun onBinValue() = Unit
}
