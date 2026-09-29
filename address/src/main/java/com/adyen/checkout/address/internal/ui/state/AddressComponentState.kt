/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.getPaymentDataValue

internal data class AddressComponentState(
    val countries: List<CountryModel>,
    val country: CountryInputState,
    val postalCode: TextInputComponentState,
    val focusRequest: FocusRequest<AddressFormElementId>? = null,
) : ComponentState {

    val form: FormState<AddressFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(
            elements = listOfNotNull(
                FormElementState(AddressFormElementId.COUNTRY, isValid = country.error == null),
                postalCode.toFormElementIfVisible(AddressFormElementId.POSTAL_CODE),
            ),
        )
    }
}

internal fun AddressComponentState.updateTextInput(
    id: AddressFormElementId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): AddressComponentState = when (id) {
    AddressFormElementId.POSTAL_CODE -> copy(postalCode = transform(postalCode))
    AddressFormElementId.COUNTRY -> this
}

internal fun AddressComponentState.toAddressModel() = AddressModel(
    country = country.selectedCountry?.isoCode,
    postalCode = postalCode.getPaymentDataValue(),
)
