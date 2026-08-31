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
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal data class MBWayComponentState(
    val countries: List<CountryModel>,
    val selectedCountryCode: CountryModel,
    val phoneNumber: TextInputComponentState,
    val isLoading: Boolean,
    // A focus move the state layer is asking the UI to make. Unlike the field order this is not derivable, since it
    // records something that happened rather than something that is.
    val focusRequest: FocusRequest<MBWayFieldId>? = null,
) : ComponentState {

    /**
     * Which fields are on screen and in which order, plus any pending focus move. Both MB Way fields are always shown,
     * so unlike card there is nothing to derive.
     */
    val form: FormState<MBWayFieldId> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormState(
            order = listOf(MBWayFieldId.COUNTRY_CODE, MBWayFieldId.PHONE_NUMBER),
            focusRequest = focusRequest,
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
    id: MBWayFieldId,
    transform: (TextInputComponentState) -> TextInputComponentState,
): MBWayComponentState = when (id) {
    MBWayFieldId.PHONE_NUMBER -> copy(phoneNumber = transform(phoneNumber))
    MBWayFieldId.COUNTRY_CODE -> this
}
