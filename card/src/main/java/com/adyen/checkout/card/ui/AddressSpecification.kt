/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/10/2022.
 *
 */

package com.adyen.checkout.card.ui

import com.adyen.checkout.card.R

/**
 * Specification for address form alternatives depending on the country.
 */
@Suppress("LongParameterList")
enum class AddressSpecification(
    internal val street: AddressFieldSpec,
    internal val houseNumber: AddressFieldSpec,
    internal val apartmentSuite: AddressFieldSpec,
    internal val postalCode: AddressFieldSpec,
    internal val city: AddressFieldSpec,
    internal val stateProvince: AddressFieldSpec,
    internal val country: AddressFieldSpec
) {
    // Brazil
    BR(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_StreetInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_StreetInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_HouseNumberInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_HouseNumberInput_Optional
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput_Optional
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput_Optional
        )
    ),

    // Canada
    CA(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_AddressInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_AddressInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_ProvinceTerritoryInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ProvinceTerritoryInput_Optional
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput_Optional
        )
    ),

    // Great Britain
    GB(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_StreetInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_StreetInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_HouseNumberInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_HouseNumberInput_Optional
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_CityTownInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_CityTownInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput_Optional
        )
    ),

    // United States
    US(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_AddressInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_AddressInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_ZipCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ZipCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput_Optional
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput_Optional
        )
    ),

    // Default
    DEFAULT(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_StreetInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_StreetInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_HouseNumberInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_HouseNumberInput_Optional
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_Card_ProvinceTerritoryInput,
            optionalStyleResId = R.style.AdyenCheckout_Card_ProvinceTerritoryInput_Optional
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput
        )
    );

    companion object {
        fun fromString(countryCode: String?): AddressSpecification {
            return values().firstOrNull { it.name == countryCode } ?: DEFAULT
        }
    }

    internal data class AddressFieldSpec(
        val isRequired: Boolean,
        val styleResId: Int,
        val optionalStyleResId: Int
    )
}
