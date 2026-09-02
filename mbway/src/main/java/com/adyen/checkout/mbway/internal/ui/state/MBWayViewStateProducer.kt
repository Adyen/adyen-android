/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class MBWayViewStateProducer(
    private val amount: Amount?,
    private val showSubmitButton: Boolean,
) : ViewStateProducer<MBWayComponentState, MBWayViewState> {

    override fun produce(state: MBWayComponentState) = MBWayViewState(
        // The form decides which fields are shown and in which order, so building one element per member of that order
        // is the only place either question is answered.
        elements = state.form.elements.map { state.toElement(it.id) },
        isLoading = state.isLoading,
        payButtonViewState = if (showSubmitButton) PayButtonViewState(amount, state.isLoading) else null,
        countryPickerViewState = CountryPickerViewState(
            countries = state.countries,
            selectedCountry = state.selectedCountryCode,
        ),
    )

    private fun MBWayComponentState.toElement(id: MBWayFormElementId): MBWayFormElement = when (id) {
        MBWayFormElementId.COUNTRY_CODE -> MBWayFormElement.CountryCode(selectedCountry = selectedCountryCode)

        MBWayFormElementId.PHONE_NUMBER -> MBWayFormElement.PhoneNumber(
            textInputViewState = phoneNumber.toViewState(form, focusRequest, id),
            callingCode = selectedCountryCode.callingCode,
        )
    }
}
