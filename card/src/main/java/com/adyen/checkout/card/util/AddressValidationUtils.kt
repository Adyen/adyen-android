package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.R
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

object AddressValidationUtils {

    /**
     * Validate address input.
     */
    fun validateAddressInput(addressInputModel: AddressInputModel, addressFormUIState: AddressFormUIState): AddressOutputData {
        return when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> validateAddressInput(addressInputModel)
            AddressFormUIState.POSTAL_CODE -> validatePostalCode(addressInputModel)
            else -> makeValidEmptyAddressOutput(addressInputModel)
        }
    }

    private fun validatePostalCode(addressInputModel: AddressInputModel): AddressOutputData {
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode),
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
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode),
                street = validateAddressField(street),
                stateOrProvince = validateAddressField(stateOrProvince),
                houseNumberOrName = validateAddressField(houseNumberOrName),
                apartmentSuite = FieldState(apartmentSuite, Validation.Valid),
                city = validateAddressField(city),
                country = validateAddressField(country)
            )
        }
    }

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

    private fun validateAddressField(input: String): FieldState<String> {
        return if (input.isNotEmpty()) {
            FieldState(input, Validation.Valid)
        } else {
            FieldState(input, Validation.Invalid(R.string.checkout_social_security_number_not_valid)) // TODO correct translation
        }
    }
}
