package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.AddressFormInput
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

object AddressValidationUtils {

    /**
     * Validate address input.
     */
    fun validateAddressInput(
        addressInputModel: AddressInputModel,
        addressFormUIState: AddressFormUIState
    ): AddressOutputData {
        return when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> validateAddressInput(addressInputModel)
            AddressFormUIState.POSTAL_CODE -> validatePostalCode(addressInputModel)
            else -> makeValidEmptyAddressOutput(addressInputModel)
        }
    }

    private fun validatePostalCode(addressInputModel: AddressInputModel): AddressOutputData {
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode, true),
                street = FieldState(street, Validation.Valid),
                stateOrProvince = FieldState(stateOrProvince, Validation.Valid),
                houseNumberOrName = FieldState(houseNumberOrName, Validation.Valid),
                apartmentSuite = FieldState(apartmentSuite, Validation.Valid),
                city = FieldState(city, Validation.Valid),
                country = FieldState(country, Validation.Valid)
            )
        }
    }

    private fun validateAddressInput(addressInputModel: AddressInputModel): AddressOutputData {
        val spec = AddressFormInput.AddressSpecification.fromString(addressInputModel.country)
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode, spec.postalCode.isRequired),
                street = validateAddressField(street, spec.street.isRequired),
                stateOrProvince = validateAddressField(stateOrProvince, spec.stateProvince.isRequired),
                houseNumberOrName = validateAddressField(houseNumberOrName, spec.houseNumber.isRequired),
                apartmentSuite = validateAddressField(apartmentSuite, spec.apartmentSuite.isRequired),
                city = validateAddressField(city, spec.city.isRequired),
                country = validateAddressField(country, spec.country.isRequired)
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
                country = FieldState(country, Validation.Valid)
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
