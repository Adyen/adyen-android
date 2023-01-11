package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.AddressSpecification
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

@Suppress("LongParameterList")
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
            AddressFormUIState.FULL_ADDRESS -> validateAddressInput(
                addressInputModel,
                isOptional,
                countryOptions,
                stateOptions
            )
            AddressFormUIState.POSTAL_CODE -> validatePostalCode(
                addressInputModel,
                isOptional,
                countryOptions,
                stateOptions
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
                stateOptions = stateOptions
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
                stateOptions = stateOptions
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
                stateOptions = emptyList()
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
