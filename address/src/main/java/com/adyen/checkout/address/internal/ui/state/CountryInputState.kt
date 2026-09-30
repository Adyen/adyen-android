/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * The country picked in the address form. Unlike a text input it cannot take focus, so its error is only shown when
 * the shopper confirms the address.
 */
internal data class CountryInputState(
    val selectedCountry: CountryModel? = null,
    val error: TextInputComponentState.InputError? = null,
) {

    /** Replaces the error, keeping whether it is currently visible. */
    fun updateError(message: CheckoutLocalizationKey?) = copy(
        error = message?.let { TextInputComponentState.InputError(it, isVisible = error?.isVisible == true) },
    )

    fun showErrorIfPresent() = copy(error = error?.copy(isVisible = true))
}
