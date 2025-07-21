/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import androidx.annotation.RestrictTo
import androidx.annotation.StyleRes
import com.adyen.checkout.ui.core.R

/**
 * Specification for address form alternatives depending on the country.
 */
@Suppress("LongParameterList")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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
            styleResId = R.style.AdyenCheckout_StreetInput,
            optionalStyleResId = R.style.AdyenCheckout_StreetInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_HouseNumberInput,
            optionalStyleResId = R.style.AdyenCheckout_HouseNumberInput_Optional
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput,
            optionalStyleResId = null
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = null
        )
    ),

    // Canada
    CA(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_AddressInput,
            optionalStyleResId = R.style.AdyenCheckout_AddressInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_ProvinceTerritoryInput,
            optionalStyleResId = null
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = null
        )
    ),

    // Great Britain
    GB(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_StreetInput,
            optionalStyleResId = R.style.AdyenCheckout_StreetInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_HouseNumberInput,
            optionalStyleResId = R.style.AdyenCheckout_HouseNumberInput_Optional
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_CityTownInput,
            optionalStyleResId = R.style.AdyenCheckout_CityTownInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = null
        )
    ),

    // United States
    US(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_AddressInput,
            optionalStyleResId = R.style.AdyenCheckout_AddressInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = false,
            styleResId = 0,
            optionalStyleResId = 0
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_ZipCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_ZipCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput,
            optionalStyleResId = null
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = null
        )
    ),

    // Default
    DEFAULT(
        street = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_StreetInput,
            optionalStyleResId = R.style.AdyenCheckout_StreetInput_Optional
        ),
        houseNumber = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_HouseNumberInput,
            optionalStyleResId = R.style.AdyenCheckout_HouseNumberInput_Optional
        ),
        apartmentSuite = AddressFieldSpec(
            isRequired = false,
            styleResId = R.style.AdyenCheckout_ApartmentSuiteInput,
            optionalStyleResId = R.style.AdyenCheckout_ApartmentSuiteInput_Optional
        ),
        postalCode = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_PostalCodeInput,
            optionalStyleResId = R.style.AdyenCheckout_PostalCodeInput_Optional
        ),
        city = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_CityInput,
            optionalStyleResId = R.style.AdyenCheckout_CityInput_Optional
        ),
        stateProvince = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_ProvinceTerritoryInput,
            optionalStyleResId = R.style.AdyenCheckout_ProvinceTerritoryInput_Optional
        ),
        country = AddressFieldSpec(
            isRequired = true,
            styleResId = R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput,
            optionalStyleResId = null
        )
    );

    companion object {
        fun fromString(countryCode: String?): AddressSpecification {
            return entries.firstOrNull { it.name == countryCode } ?: DEFAULT
        }
    }

    internal data class AddressFieldSpec(
        val isRequired: Boolean,
        @StyleRes val styleResId: Int,
        @StyleRes val optionalStyleResId: Int?
    ) {
        fun getStyleResId(isOptional: Boolean): Int? {
            return if (isOptional) {
                optionalStyleResId
            } else {
                styleResId
            }
        }
    }
}
