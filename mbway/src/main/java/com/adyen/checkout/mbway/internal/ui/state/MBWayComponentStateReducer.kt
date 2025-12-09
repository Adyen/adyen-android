/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class MBWayComponentStateReducer : ComponentStateReducer<MBWayComponentState, MBWayIntent> {

    override fun reduce(state: MBWayComponentState, intent: MBWayIntent): MBWayComponentState {
        return when (intent) {
            is MBWayIntent.UpdateCountry -> state.copy(countryCode = intent.country)
            is MBWayIntent.UpdateLoading -> state.copy(isLoading = intent.isLoading)
            is MBWayIntent.UpdatePhoneNumber -> state.copy(
                phoneNumber = state.phoneNumber.updateText(intent.number),
            )

            is MBWayIntent.UpdatePhoneNumberFocus -> state.copy(
                phoneNumber = state.phoneNumber.updateFocus(intent.hasFocus),
            )

            is MBWayIntent.HighlightValidationErrors -> {
                val hasPhoneNumberError = state.phoneNumber.errorMessage != null
                state.copy(
                    phoneNumber = state.phoneNumber.copy(
                        showError = hasPhoneNumberError,
                        isFocused = hasPhoneNumberError,
                    ),
                )
            }
        }
    }
}
