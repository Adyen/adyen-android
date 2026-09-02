/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.toFormElementIfVisible
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class MBWayComponentState(
    val countries: List<CountryModel>,
    val selectedCountryCode: CountryModel,
    val phoneNumber: TextInputComponentState,
    val isLoading: Boolean,
    val focusRequest: FocusRequest<MBWayFormElementId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order, plus any pending focus move. Both MB Way fields are always shown,
     * so unlike card there is nothing to derive.
     */
    val form: FormState<MBWayFormElementId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(
            elements = listOfNotNull(
                // The picker always holds a country, so it can never hold up a payment.
                FormElementState(MBWayFormElementId.COUNTRY_CODE, isValid = true),
                phoneNumber.toFormElementIfVisible(MBWayFormElementId.PHONE_NUMBER),
            ),
        )
    }
}

/**
 * Applies [transform] to the text input [id] names. The country code is a picker, so it has nothing to transform and
 * leaves the state unchanged.
 *
 * It lives next to the state it updates so that adding a field above does not compile until it is mapped here.
 */
internal fun MBWayComponentState.updateTextInput(
    id: MBWayFormElementId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): MBWayComponentState = when (id) {
    MBWayFormElementId.PHONE_NUMBER -> copy(phoneNumber = transform(phoneNumber))
    MBWayFormElementId.COUNTRY_CODE -> this
}
