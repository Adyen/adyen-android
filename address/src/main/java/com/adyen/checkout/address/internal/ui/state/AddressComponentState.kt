/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.getPaymentDataValue

/**
 * @param countries The countries the shopper can pick. Empty until they are loaded, and when loading them failed.
 * @param country The ISO 3166-1 alpha-2 code of the selected country. It can be selected before [countries] load.
 * @param states The states of the selected country. Empty until they are loaded, when loading them failed, and for a
 * country without states.
 * @param stateOrProvince The ISO 3166-2 code of the selected state. It can be selected before [states] load. Only a
 * [spec] with states has a state element.
 */
internal data class AddressComponentState(
    val countries: List<AddressRegion>,
    val country: PickerInputComponentState<String>,
    val street: TextInputComponentState,
    val houseNumberOrName: TextInputComponentState,
    val postalCode: TextInputComponentState,
    val city: TextInputComponentState,
    val states: List<AddressRegion>,
    val stateOrProvince: PickerInputComponentState<String>,
    val focusRequest: FocusRequest<AddressFormElementId>? = null,
) : ComponentState {

    /**
     * The layout of the selected country, or the default layout when none is selected. It is derived from [country]
     * and never stored, so the two cannot drift; cached like [form], which reads it.
     */
    val spec: AddressSpec by lazy(LazyThreadSafetyMode.PUBLICATION) {
        AddressSpec.fromCountryCode(country.selected)
    }

    /**
     * The visible elements in the order the shopper sees them, which is the order of [spec], starting with the country.
     * This form is derived from the fields above and never manually created or modified.
     *
     * It is cached because the reducer, the validator, the post processor and the producer all read it.
     * [LazyThreadSafetyMode.PUBLICATION] is used because the same state might be read from several threads.
     */
    val form: FormState<AddressFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(
            elements = spec.order.mapNotNull { id -> getFormElement(id) },
        )
    }

    private fun getFormElement(id: AddressFormElementId): FormElementState<AddressFormElementId>? = when (id) {
        AddressFormElementId.COUNTRY -> country.toFormElement(id)
        AddressFormElementId.STREET -> street.toFormElementIfVisible(id)
        AddressFormElementId.HOUSE_NUMBER_OR_NAME -> houseNumberOrName.toFormElementIfVisible(id)
        AddressFormElementId.POSTAL_CODE -> postalCode.toFormElementIfVisible(id)
        AddressFormElementId.CITY -> city.toFormElementIfVisible(id)
        // A hidden state is left out like a hidden text input.
        AddressFormElementId.STATE_OR_PROVINCE -> stateOrProvince.toFormElement(id).takeIf { spec.hasStates }
    }
}

/**
 * The address the shopper confirms: the selected country, the trimmed text of the text fields with null for a blank
 * one, and the selected state for a country that has states.
 */
internal fun AddressComponentState.toAddressModel() = AddressModel(
    country = country.selected,
    street = street.getTrimmedValue(),
    houseNumberOrName = houseNumberOrName.getTrimmedValue(),
    postalCode = postalCode.getTrimmedValue(),
    city = city.getTrimmedValue(),
    stateOrProvince = stateOrProvince.selected?.takeIf { spec.hasStates },
)

private fun TextInputComponentState.getTrimmedValue(): String? = getPaymentDataValue()?.trim()

/**
 * Updates the text input corresponding to the provided [id]. Elements that are not text inputs are ignored.
 */
internal fun AddressComponentState.updateTextInput(
    id: AddressFormElementId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): AddressComponentState = when (id) {
    AddressFormElementId.STREET -> copy(street = transform(street))
    AddressFormElementId.HOUSE_NUMBER_OR_NAME -> copy(houseNumberOrName = transform(houseNumberOrName))
    AddressFormElementId.POSTAL_CODE -> copy(postalCode = transform(postalCode))
    AddressFormElementId.CITY -> copy(city = transform(city))
    AddressFormElementId.COUNTRY,
    AddressFormElementId.STATE_OR_PROVINCE -> this
}

/**
 * Updates the picker corresponding to the provided [id]. Elements that are not pickers are ignored.
 */
internal fun AddressComponentState.updatePickerInput(
    id: AddressFormElementId,
    transform: (PickerInputComponentState<String>) -> PickerInputComponentState<String>,
): AddressComponentState = when (id) {
    AddressFormElementId.COUNTRY -> copy(country = transform(country))
    AddressFormElementId.STATE_OR_PROVINCE -> copy(stateOrProvince = transform(stateOrProvince))

    AddressFormElementId.STREET,
    AddressFormElementId.HOUSE_NUMBER_OR_NAME,
    AddressFormElementId.POSTAL_CODE,
    AddressFormElementId.CITY -> this
}
