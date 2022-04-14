/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/4/2022.
 */

package com.adyen.checkout.sepa.di

import com.adyen.checkout.core.di.AppContainer
import com.adyen.checkout.core.di.DependencyContainerNode
import com.adyen.checkout.sepa.SepaProcessor

object SepaContainer : DependencyContainerNode(listOf(AppContainer)) {
    init {
        addProvider(SepaProcessor::class) { SepaProcessor }
    }
}
