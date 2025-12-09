/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel

internal sealed interface MBWayIntent {

    data class UpdateCountry(val country: CountryModel) : MBWayIntent

    data class UpdateLoading(val isLoading: Boolean) : MBWayIntent

    data class UpdatePhoneNumber(val number: String) : MBWayIntent

    data class UpdatePhoneNumberFocus(val hasFocus: Boolean) : MBWayIntent

    data object HighlightValidationErrors : MBWayIntent
}
