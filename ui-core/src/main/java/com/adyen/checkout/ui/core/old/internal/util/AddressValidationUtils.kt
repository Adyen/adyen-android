/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.AddressSpecification
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData

@Suppress("LongParameterList")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object AddressValidationUtils {

    /**
     * Validate address input.
     */
    fun validateAddressInput(
        addressInputModel: AddressInputModel,
        addressFormUIState: AddressFormUIState,
        countryOptions: List<AddressListItem>,
        stateOptions: List<AddressListItem>,
        isOptional: Boolean,
    ): AddressOutputData {
        return when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS, AddressFormUIState.LOOKUP -> validateAddressInput(
                addressInputModel,
                isOptional,
                countryOptions,
                stateOptions,
            )

            AddressFormUIState.POSTAL_CODE -> validatePostalCode(
                addressInputModel,
                isOptional,
                countryOptions,
                stateOptions,
            )

            else -> makeValidEmptyAddressOutput(addressInputModel)
        }
    }

    private fun validatePostalCode(
        addressInputModel: AddressInputModel,
        isOptional: Boolean,
        countryOptions: List<AddressListItem>,
        stateOptions: List<AddressListItem>,
    ): AddressOutputData {
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode, !isOptional),
                street = FieldState(street, Validation.Valid),
                stateOrProvince = FieldState(stateOrProvince, Validation.Valid),
                houseNumberOrName = FieldState(houseNumberOrName, Validation.Valid),
                apartmentSuite = FieldState(apartmentSuite, Validation.Valid),
                city = FieldState(city, Validation.Valid),
                country = FieldState(country, Validation.Valid),
                isOptional = isOptional,
                countryOptions = countryOptions,
                stateOptions = stateOptions,
                countryDisplayName = countryDisplayName,
            )
        }
    }

    private fun validateAddressInput(
        addressInputModel: AddressInputModel,
        isOptional: Boolean,
        countryOptions: List<AddressListItem>,
        stateOptions: List<AddressListItem>,
    ): AddressOutputData {
        val spec = AddressSpecification.fromString(addressInputModel.country)
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode, spec.postalCode.isRequired && !isOptional),
                street = validateAddressField(street, spec.street.isRequired && !isOptional),
                stateOrProvince = validateAddressField(stateOrProvince, spec.stateProvince.isRequired && !isOptional),
                houseNumberOrName = validateAddressField(houseNumberOrName, spec.houseNumber.isRequired && !isOptional),
                apartmentSuite = validateAddressField(apartmentSuite, spec.apartmentSuite.isRequired && !isOptional),
                city = validateAddressField(city, spec.city.isRequired && !isOptional),
                country = validateAddressField(country, spec.country.isRequired && !isOptional),
                isOptional = isOptional,
                countryOptions = countryOptions,
                stateOptions = stateOptions,
                countryDisplayName = countryDisplayName,
            )
        }
    }

    /**
     * Make [AddressOutputData] without validating any fields.
     */
    fun makeValidEmptyAddressOutput(addressInputModel: AddressInputModel): AddressOutputData {
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = FieldState(postalCode, Validation.Valid),
                street = FieldState(street, Validation.Valid),
                stateOrProvince = FieldState(stateOrProvince, Validation.Valid),
                houseNumberOrName = FieldState(houseNumberOrName, Validation.Valid),
                apartmentSuite = FieldState(apartmentSuite, Validation.Valid),
                city = FieldState(city, Validation.Valid),
                country = FieldState(country, Validation.Valid),
                isOptional = true,
                countryOptions = emptyList(),
                stateOptions = emptyList(),
                countryDisplayName = countryDisplayName,
            )
        }
    }

    private fun validateAddressField(input: String, shouldValidate: Boolean): FieldState<String> {
        return if (input.isNotEmpty() || !shouldValidate) {
            FieldState(input, Validation.Valid)
        } else {
            FieldState(input, Validation.Invalid(R.string.checkout_address_form_field_not_valid))
        }
    }
}
