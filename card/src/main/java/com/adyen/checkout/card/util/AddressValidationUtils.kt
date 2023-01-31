package com.adyen.checkout.card.util

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.AddressFormUIState
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.R
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.ui.AddressSpecification
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

object AddressValidationUtils {

    /**
     * Validate address input.
     */
    fun validateAddressInput(
        addressInputModel: AddressInputModel,
        addressFormUIState: AddressFormUIState,
        addressConfiguration: AddressConfiguration?,
        detectedCardType: DetectedCardType?,
    ): AddressOutputData {
        val isOptional = isOptional(addressConfiguration, detectedCardType)
        return when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> validateAddressInput(addressInputModel, isOptional)
            AddressFormUIState.POSTAL_CODE -> validatePostalCode(addressInputModel, isOptional)
            else -> makeValidEmptyAddressOutput(addressInputModel)
        }
    }

    private fun validatePostalCode(addressInputModel: AddressInputModel, isOptional: Boolean): AddressOutputData {
        return with(addressInputModel) {
            AddressOutputData(
                postalCode = validateAddressField(postalCode, !isOptional),
                street = FieldState(street, Validation.Valid),
                stateOrProvince = FieldState(stateOrProvince, Validation.Valid),
                houseNumberOrName = FieldState(houseNumberOrName, Validation.Valid),
                apartmentSuite = FieldState(apartmentSuite, Validation.Valid),
                city = FieldState(city, Validation.Valid),
                country = FieldState(country, Validation.Valid),
                isOptional = isOptional
            )
        }
    }

    private fun validateAddressInput(addressInputModel: AddressInputModel, isOptional: Boolean): AddressOutputData {
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
                isOptional = isOptional
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
                isOptional = true
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

    private fun isOptional(addressConfiguration: AddressConfiguration?, detectedCardType: DetectedCardType?): Boolean {
        return when (addressConfiguration) {
            AddressConfiguration.None -> true
            is AddressConfiguration.PostalCode -> isOptional(addressConfiguration.addressFieldPolicy, detectedCardType)
            is AddressConfiguration.FullAddress -> false
            else -> true
        }
    }

    private fun isOptional(policy: AddressConfiguration.AddressFieldPolicy, detectedCardType: DetectedCardType?): Boolean {
        return when (policy) {
            is AddressConfiguration.AddressFieldPolicy.Required -> false
            is AddressConfiguration.AddressFieldPolicy.Optional -> true
            is AddressConfiguration.AddressFieldPolicy.OptionalForCardTypes -> {
                if (detectedCardType == null) {
                    false
                } else {
                    policy.brands.contains(detectedCardType.cardBrand.txVariant)
                }
            }
        }
    }
}
