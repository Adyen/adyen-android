/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/3/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.InputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class PhoneNumberInputData(
    var countryCode: String = "",
    var everythingAfterCountryCode: String = "",
) : InputData
