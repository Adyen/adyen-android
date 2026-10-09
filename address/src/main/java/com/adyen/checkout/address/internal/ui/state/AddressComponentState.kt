/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressField
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * @param spec The layout of the selected country, or the default layout when none is selected.
 * @param countries The countries the shopper can pick. Empty until they are loaded, and when loading them failed.
 * @param country The ISO 3166-1 alpha-2 code of the selected country. It can be selected before [countries] load.
 */
internal data class AddressComponentState(
    val spec: AddressSpec,
    val countries: List<AddressRegion>,
    val isLoadingCountries: Boolean,
    val country: PickerInputComponentState<String>,
    val street: TextInputComponentState,
    val houseNumberOrName: TextInputComponentState,
    val postalCode: TextInputComponentState,
    val city: TextInputComponentState,
    val stateOrProvince: StateOrProvinceState,
    val focusRequest: FocusRequest<AddressFormElementId>? = null,
) : ComponentState {

    /**
     * The visible elements in the order the shopper sees them: the country, then the fields in the order of [spec].
     * This form is derived from the fields above and never manually created or modified.
     *
     * It is cached because the reducer, the validator, the post processor and the producer all read it.
     * [LazyThreadSafetyMode.PUBLICATION] is used because the same state might be read from several threads.
     */
    val form: FormState<AddressFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val countryElement = country.toFormElement(AddressFormElementId.COUNTRY)
        FormState(
            elements = listOf(countryElement) + spec.order.mapNotNull { field -> getFormElement(field) },
        )
    }

    private fun getFormElement(field: AddressField): FormElementState<AddressFormElementId>? = when (field) {
        AddressField.STREET -> street.toFormElementIfVisible(AddressFormElementId.STREET)
        AddressField.HOUSE_NUMBER_OR_NAME ->
            houseNumberOrName.toFormElementIfVisible(AddressFormElementId.HOUSE_NUMBER_OR_NAME)

        AddressField.POSTAL_CODE -> postalCode.toFormElementIfVisible(AddressFormElementId.POSTAL_CODE)
        AddressField.CITY -> city.toFormElementIfVisible(AddressFormElementId.CITY)
        AddressField.STATE_OR_PROVINCE -> when (stateOrProvince) {
            is StateOrProvinceState.Loading,
            StateOrProvinceState.Unavailable -> null

            is StateOrProvinceState.Options ->
                stateOrProvince.picker.toFormElement(AddressFormElementId.STATE_OR_PROVINCE_PICKER)

            is StateOrProvinceState.FreeText ->
                stateOrProvince.input.toFormElementIfVisible(AddressFormElementId.STATE_OR_PROVINCE_TEXT)
        }
    }
}
