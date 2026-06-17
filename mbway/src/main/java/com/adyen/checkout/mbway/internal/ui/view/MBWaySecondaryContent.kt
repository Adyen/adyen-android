/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/6/2026.
 */

package com.adyen.checkout.mbway.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun MBWaySecondaryContent(
    modifier: Modifier,
    identifier: String,
    viewState: StateFlow<MBWayViewState>,
    onIntent: (MBWayIntent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val viewState by viewState.collectAsStateWithLifecycle()

    when (identifier) {
        MBWaySecondaryContentEntry.COUNTRY_CODE_PICKER -> {
            CountryCodePicker(
                viewState = viewState,
                onItemClick = {
                    onIntent(MBWayIntent.UpdateCountry(it))
                    onDismissRequest()
                },
                modifier = modifier,
            )
        }
    }
}

internal object MBWaySecondaryContentEntry {
    const val COUNTRY_CODE_PICKER = "COUNTRY_CODE_PICKER"
}
