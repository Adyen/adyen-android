/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/9/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel

internal interface MBWayChangeListener {

    fun onCountryChanged(newCountryCode: CountryModel)

    fun onPhoneNumberChanged(newPhoneNumber: String)

    fun onPhoneNumberFocusChanged(hasFocus: Boolean)
}
