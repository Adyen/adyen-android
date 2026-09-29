/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.address.internal.ui.state.AddressViewState
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun AddressSecondaryContent(
    identifier: String,
    viewStateFlow: StateFlow<AddressViewState>,
    onCountryClick: (CountryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()

    when (identifier) {
        AddressSecondaryContentEntry.COUNTRY_PICKER -> CountryPicker(
            viewState = viewState.countryPickerViewState,
            onItemClick = onCountryClick,
            modifier = modifier,
        )
    }
}

/**
 * The keys of the address component's secondary screens. They share the back stack of the component hosting the
 * address form, so they are prefixed to stay clear of the host's own keys.
 */
internal object AddressSecondaryContentEntry {
    const val COUNTRY_PICKER = "ADDRESS_COUNTRY_PICKER"
}
