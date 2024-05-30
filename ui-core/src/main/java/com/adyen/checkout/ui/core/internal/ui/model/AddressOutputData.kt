/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/3/2022.
 */

package com.adyen.checkout.ui.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AddressOutputData(
    val postalCode: FieldState<String>,
    val street: FieldState<String>,
    val stateOrProvince: FieldState<String>,
    val houseNumberOrName: FieldState<String>,
    val apartmentSuite: FieldState<String>,
    val city: FieldState<String>,
    val country: FieldState<String>,
    val isOptional: Boolean,
    val countryOptions: List<AddressListItem>,
    val stateOptions: List<AddressListItem>
) : OutputData {
    override val isValid: Boolean
        get() = postalCode.validation.isValid() &&
            street.validation.isValid() &&
            stateOrProvince.validation.isValid() &&
            houseNumberOrName.validation.isValid() &&
            apartmentSuite.validation.isValid() &&
            city.validation.isValid() &&
            country.validation.isValid()

    override fun toString(): String {
        return listOf(
            street.value,
            houseNumberOrName.value,
            apartmentSuite.value,
            postalCode.value,
            city.value,
            stateOrProvince.value,
            country.value,
        ).filter { it.isNotBlank() }.joinToString(" ")
    }
}
