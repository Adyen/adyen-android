/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.address.internal.ui.state.AddressIntent
import com.adyen.checkout.address.internal.ui.state.AddressViewState
import com.adyen.checkout.address.internal.ui.state.PickerViewState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun AddressSecondaryContent(
    identifier: String,
    viewStateFlow: StateFlow<AddressViewState>,
    onIntent: (AddressIntent) -> Unit,
    onCountryClick: () -> Unit,
    onStateOrProvinceClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCountrySelected: (String) -> Unit,
    onStateOrProvinceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()

    when (AddressSecondaryContentEntry.fromKey(identifier)) {
        AddressSecondaryContentEntry.FORM -> AddressFormContent(
            viewState = viewState,
            onIntent = onIntent,
            onCountryClick = onCountryClick,
            onStateOrProvinceClick = onStateOrProvinceClick,
            onSaveClick = onSaveClick,
            modifier = modifier,
        )

        AddressSecondaryContentEntry.COUNTRY_PICKER -> AddressRegionPicker(
            viewState = viewState.countryPickerViewState,
            onItemClick = onCountrySelected,
            modifier = modifier,
        )

        AddressSecondaryContentEntry.STATE_PICKER -> StateOrProvincePicker(
            statePickerViewState = viewState.statePickerViewState,
            onItemClick = onStateOrProvinceSelected,
            modifier = modifier,
        )

        null -> Unit
    }
}

@Composable
private fun StateOrProvincePicker(
    statePickerViewState: PickerViewState?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Without states the row that opens this screen is not on the address form either, so this is only reached when
    // process death restores the screen on a form whose country has no states. Back leaves it.
    if (statePickerViewState == null) return
    AddressRegionPicker(
        viewState = statePickerViewState,
        onItemClick = onItemClick,
        modifier = modifier,
    )
}

/**
 * The address component's screens. Their keys share the host's back stack, so they are prefixed to stay clear of the
 * host's keys, and a host supports one address component.
 */
internal enum class AddressSecondaryContentEntry(val key: String) {
    FORM("ADDRESS_FORM"),
    COUNTRY_PICKER("ADDRESS_COUNTRY_PICKER"),
    STATE_PICKER("ADDRESS_STATE_PICKER");

    companion object {
        fun fromKey(key: String): AddressSecondaryContentEntry? = entries.find { it.key == key }
    }
}
