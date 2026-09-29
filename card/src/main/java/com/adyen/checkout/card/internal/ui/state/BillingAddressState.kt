/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * The billing address the shopper confirmed on the address form. The form itself belongs to the address component; the
 * card only keeps what was confirmed, to submit it and to prefill the form when the shopper returns to edit it.
 *
 * @param isVisible Whether the card form asks for a billing address.
 * @param address The confirmed address, or null while none is confirmed.
 */
internal data class BillingAddressState(
    val isVisible: Boolean = false,
    val address: AddressModel? = null,
    val error: TextInputComponentState.InputError? = null,
) {

    /** Replaces the error, keeping whether it is currently visible. */
    fun updateError(message: CheckoutLocalizationKey?) = copy(
        error = message?.let { TextInputComponentState.InputError(it, isVisible = error?.isVisible == true) },
    )

    fun showErrorIfPresent() = copy(error = error?.copy(isVisible = true))
}

/**
 * Returns the billing address row as a form element, or null when the card form does not ask for it.
 */
internal fun BillingAddressState.toFormElementIfVisible(id: CardFormElementId): FormElementState<CardFormElementId>? =
    if (isVisible) FormElementState(id, isValid = error == null) else null
