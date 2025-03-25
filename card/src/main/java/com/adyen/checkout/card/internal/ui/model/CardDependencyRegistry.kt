/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.validation.StateDependencyRegistry

class CardDependencyRegistry : StateDependencyRegistry<CardFieldId, CardDelegateState> {

    private val dependencies: Map<CardFieldId, List<(CardDelegateState) -> Any?>> =
        CardFieldId.entries.associateWith { fieldId ->
            when (fieldId) {
                CardFieldId.CARD_NUMBER -> listOf(
                    { state -> state.enableLuhnCheck },
                    { state -> state.isBrandSupported },
                    { state -> state.detectedCardTypes },
                    { state -> state.cardBrands },
                )

                else -> {
                    listOf()
                }
            }
        }

    override fun getDependencies(fieldId: CardFieldId): List<(CardDelegateState) -> Any?>? {
        return dependencies[fieldId]
    }
}
