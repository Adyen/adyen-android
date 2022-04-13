/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/4/2022.
 */

package com.adyen.checkout.core.di

import kotlin.reflect.KClass

object CheckoutServiceLocator {

    private val containers = mutableSetOf<DependencyContainer>()

    fun addContainer(dependencyContainer: DependencyContainer) {
        containers.add(dependencyContainer)
    }

    fun <T> provide(kClass: KClass<*>): T {
        val container = containers.firstOrNull { it.canProvide(kClass) }
            ?: throw IllegalArgumentException("Cannot find container with provider for ${kClass.simpleName}")
        return container.provide(kClass)
    }
}
