/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

/**
 * The address form layout per country: the order of the fields, starting with the country, their labels and which of
 * them are optional.
 *
 * Every spec lists every field; a field a country does not use is hidden. Only the countries that have states require
 * [AddressFormElementId.STATE_OR_PROVINCE], as Web's `hasDataset` (`adyen-web`
 * `packages/lib/src/components/internal/Address/constants.ts`), and their states are requested; every other country
 * lists it as hidden.
 */
internal enum class AddressSpec(
    val fields: List<AddressFieldSpec>,
) {
    DEFAULT(
        listOf(
            country(),
            street(),
            houseNumberOrName(),
            postalCode(),
            city(),
            stateOrProvince(requirementPolicy = RequirementPolicy.Hidden),
        ),
    ),
    AU(
        listOf(
            country(),
            street(label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL_ADDRESS),
            houseNumberOrName(
                label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
                requirementPolicy = RequirementPolicy.Optional,
            ),
            city(),
            stateOrProvince(label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE),
            postalCode(),
        ),
    ),
    BR(
        listOf(
            country(),
            street(),
            houseNumberOrName(),
            postalCode(),
            city(),
            stateOrProvince(label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE),
        ),
    ),
    CA(
        listOf(
            country(),
            street(label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL_ADDRESS),
            houseNumberOrName(
                label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
                requirementPolicy = RequirementPolicy.Optional,
            ),
            city(),
            postalCode(),
            stateOrProvince(label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_PROVINCE_OR_TERRITORY),
        ),
    ),
    GB(
        listOf(
            country(),
            houseNumberOrName(),
            street(),
            city(label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN),
            postalCode(),
            stateOrProvince(requirementPolicy = RequirementPolicy.Hidden),
        ),
    ),
    JP(
        listOf(
            country(),
            postalCode(),
            stateOrProvince(label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_PREFECTURE),
            city(label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN),
            street(label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL_NAME_AND_BLOCK_NUMBER),
            houseNumberOrName(
                label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_BUILDING_NAME_ROOM_NUMBER,
                requirementPolicy = RequirementPolicy.Optional,
            ),
        ),
    ),
    US(
        listOf(
            country(),
            street(label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL_ADDRESS),
            houseNumberOrName(
                label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
                requirementPolicy = RequirementPolicy.Optional,
            ),
            city(),
            stateOrProvince(label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE),
            postalCode(label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL_ZIP_CODE),
        ),
    ),
    ;

    val order: List<AddressFormElementId> = fields.map { it.id }

    /** Whether the country has states, which are then requested and required. */
    val hasStates: Boolean =
        getFieldSpec(AddressFormElementId.STATE_OR_PROVINCE).requirementPolicy != RequirementPolicy.Hidden

    fun getFieldSpec(id: AddressFormElementId): AddressFieldSpec = fields.first { it.id == id }

    companion object {
        fun fromCountryCode(countryCode: String?): AddressSpec = entries.find { it.name == countryCode } ?: DEFAULT
    }
}

private fun country(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
) = AddressFieldSpec(AddressFormElementId.COUNTRY, label, RequirementPolicy.Required)

private fun street(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_STREET_LABEL,
) = AddressFieldSpec(AddressFormElementId.STREET, label, RequirementPolicy.Required)

private fun houseNumberOrName(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL,
    requirementPolicy: RequirementPolicy = RequirementPolicy.Required,
) = AddressFieldSpec(AddressFormElementId.HOUSE_NUMBER_OR_NAME, label, requirementPolicy)

private fun postalCode(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL,
) = AddressFieldSpec(AddressFormElementId.POSTAL_CODE, label, RequirementPolicy.Required)

private fun city(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_CITY_LABEL,
) = AddressFieldSpec(AddressFormElementId.CITY, label, RequirementPolicy.Required)

private fun stateOrProvince(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL,
    requirementPolicy: RequirementPolicy = RequirementPolicy.Required,
) = AddressFieldSpec(AddressFormElementId.STATE_OR_PROVINCE, label, requirementPolicy)
