/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/1/2024.
 */

package com.adyen.checkout.mbway.internal.ui.model

import androidx.annotation.StringRes
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel

internal data class MBWayViewState(
    val phoneNumber: String,
    val phoneNumberError: InputError?,
    val countries: List<CountryModel>,
    val selectedCountry: CountryModel,
    val focussedView: FocussedView,
)

internal data class InputError(
    @StringRes val messageRes: Int,
    val requestFocus: Boolean = false,
)

internal enum class FocussedView {
    PHONE_NUMBER,
    // TODO: Remove after testing
    TEST,
}
