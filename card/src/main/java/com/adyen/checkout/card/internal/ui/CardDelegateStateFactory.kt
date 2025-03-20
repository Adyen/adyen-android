/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.CardDelegateState
import com.adyen.checkout.card.internal.ui.model.CardFieldId
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateStateFactory

// TODO: Move many of the hardcoded values from the delegate state to the factory
class CardDelegateStateFactory(
    private val componentParams: CardComponentParams,
    private val paymentMethod: PaymentMethod,
) : DelegateStateFactory<CardDelegateState, CardFieldId> {

    override fun createDefaultDelegateState() = CardDelegateState(
        componentParams = componentParams,
        installmentOptions = getDefaultInstallmentOptions(),
    )

    private fun getDefaultInstallmentOptions(): List<InstallmentModel> {
        val isDebit = paymentMethod.fundingSource == DEBIT_FUNDING_SOURCE
        return if (isDebit) {
            emptyList()
        } else {
            InstallmentUtils.makeInstallmentOptions(
                installmentParams = componentParams.installmentParams,
                cardBrand = null,
                isCardTypeReliable = false,
            )
        }
    }

    override fun getFieldIds(): List<CardFieldId> = CardFieldId.entries

    companion object {
        private const val DEBIT_FUNDING_SOURCE = "debit"
    }
}
