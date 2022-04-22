package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.R
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

object AddressValidationUtils {

    fun validateAddressInput(addressInputModel: AddressInputModel, addressFormUIState: AddressFormUIState): AddressOutputData {
        return when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> validateAddressInput(addressInputModel)
            AddressFormUIState.POSTAL_CODE -> validatePostalCode(addressInputModel)
            else -> makeValidEmptyAddressOutput(addressInputModel)
        }
    }

    private fun validatePostalCode(addressInputModel: AddressInputModel): AddressOutputData {
        return AddressOutputData(
            postalCode = validateAddressField(addressInputModel.postalCode),
            street = FieldState(addressInputModel.street, Validation.Valid),
            stateOrProvince = FieldState(addressInputModel.stateOrProvince, Validation.Valid),
            houseNumberOrName = FieldState(addressInputModel.houseNumberOrName, Validation.Valid),
            city = FieldState(addressInputModel.city, Validation.Valid),
            country = FieldState(addressInputModel.country, Validation.Valid)
        )
    }

    private fun validateAddressInput(addressInputModel: AddressInputModel): AddressOutputData {
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode),
                street = validateAddressField(street),
                stateOrProvince = validateAddressField(stateOrProvince),
                houseNumberOrName = validateAddressField(houseNumberOrName),
                city = validateAddressField(city),
                country = validateAddressField(country)
            )
        }
    }

    fun makeValidEmptyAddressOutput(addressInputModel: AddressInputModel): AddressOutputData {
        return AddressOutputData(
            postalCode = FieldState(addressInputModel.postalCode, Validation.Valid),
            street = FieldState(addressInputModel.street, Validation.Valid),
            stateOrProvince = FieldState(addressInputModel.stateOrProvince, Validation.Valid),
            houseNumberOrName = FieldState(addressInputModel.houseNumberOrName, Validation.Valid),
            city = FieldState(addressInputModel.city, Validation.Valid),
            country = FieldState(addressInputModel.country, Validation.Valid)
        )
    }

    private fun validateAddressField(input: String): FieldState<String> {
        return if (input.isNotEmpty()) {
            FieldState(input, Validation.Valid)
        } else {
            FieldState(input, Validation.Invalid(R.string.checkout_social_security_number_not_valid)) // TODO correct translation
        }
    }
}
