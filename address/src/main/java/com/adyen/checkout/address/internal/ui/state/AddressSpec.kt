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
 * [AddressFormElementId.STATE_OR_PROVINCE] is part of every spec. Whether it is shown is decided at runtime by the
 * states dataset of the selected country, so there is no need for per-country state lists here.
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
            stateOrProvince(),
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
            stateOrProvince(),
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
) = AddressFieldSpec(AddressFormElementId.STATE_OR_PROVINCE, label, RequirementPolicy.Required)
