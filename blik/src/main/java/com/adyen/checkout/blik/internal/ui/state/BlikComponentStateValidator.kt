/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator

internal class BlikComponentStateValidator : ComponentStateValidator<BlikComponentState> {

    override fun validate(state: BlikComponentState): BlikComponentState {
        val blikCodeError = if (!isBlikCodeValid(state.blikCode.text)) {
            CheckoutLocalizationKey.BLIK_CODE_INVALID
        } else {
            null
        }
        return state.copy(
            blikCode = state.blikCode.copy(errorMessage = blikCodeError),
        )
    }

    override fun isValid(state: BlikComponentState): Boolean {
        return state.blikCode.errorMessage == null
    }

    private fun isBlikCodeValid(blikCode: String): Boolean {
        try {
            if (blikCode.isNotEmpty()) blikCode.toInt()
        } catch (e: NumberFormatException) {
            return false
        }
        return blikCode.length == BLIK_CODE_LENGTH
    }

    companion object {
        private const val BLIK_CODE_LENGTH = 6
    }
}
