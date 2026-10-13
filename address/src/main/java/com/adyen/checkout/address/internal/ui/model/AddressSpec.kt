/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/10/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy

/**
 * The address form layout per country: the order of the fields after the country, their labels and which of them are
 * optional.
 *
 * [AddressField.STATE_OR_PROVINCE] is part of every spec. Whether it is shown is decided at runtime by the states
 * dataset of the selected country, so there is no need for per-country state lists here.
 */
internal enum class AddressSpec(
    val fields: List<AddressFieldSpec>,
) {
    DEFAULT(
        listOf(
            street(),
            houseNumberOrName(),
            postalCode(),
            city(),
            stateOrProvince(),
        ),
    ),
    BR(
        listOf(
            street(),
            houseNumberOrName(),
            postalCode(),
            city(),
            stateOrProvince(label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE),
        ),
    ),
    CA(
        listOf(
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
            houseNumberOrName(),
            street(),
            city(label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN),
            postalCode(),
            stateOrProvince(),
        ),
    ),
    US(
        listOf(
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

    val order: List<AddressField> = fields.map { it.field }

    fun getFieldSpec(field: AddressField): AddressFieldSpec = fields.first { it.field == field }

    companion object {
        fun fromCountryCode(countryCode: String?): AddressSpec = entries.find { it.name == countryCode } ?: DEFAULT
    }
}

private fun street(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_STREET_LABEL,
) = AddressFieldSpec(AddressField.STREET, label, RequirementPolicy.Required)

private fun houseNumberOrName(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL,
    requirementPolicy: RequirementPolicy = RequirementPolicy.Required,
) = AddressFieldSpec(AddressField.HOUSE_NUMBER_OR_NAME, label, requirementPolicy)

private fun postalCode(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL,
) = AddressFieldSpec(AddressField.POSTAL_CODE, label, RequirementPolicy.Required)

private fun city(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_CITY_LABEL,
) = AddressFieldSpec(AddressField.CITY, label, RequirementPolicy.Required)

private fun stateOrProvince(
    label: CheckoutLocalizationKey = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL,
) = AddressFieldSpec(AddressField.STATE_OR_PROVINCE, label, RequirementPolicy.Required)
