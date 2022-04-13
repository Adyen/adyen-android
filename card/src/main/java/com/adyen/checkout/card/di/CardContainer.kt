/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/4/2022.
 */

package com.adyen.checkout.card.di

import com.adyen.checkout.card.CardValidationMapper
import com.adyen.checkout.core.di.AppContainer
import com.adyen.checkout.core.di.DependencyContainerNode

object CardContainer : DependencyContainerNode(listOf(AppContainer)) {
    init {
        addProvider(CardValidationMapper::class) { CardValidationMapper }
    }
}
