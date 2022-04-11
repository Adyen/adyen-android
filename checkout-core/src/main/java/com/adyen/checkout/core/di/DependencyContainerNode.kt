/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.core.di

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class DependencyContainerNode(private val parentContainers: List<DependencyContainerNode>) :
    DependencyContainer {

    protected val providerMap: HashMap<KClass<*>, DependencyProvider<*>> = HashMap()

    override fun canProvide(kClass: KClass<*>): Boolean {
        return if (hasProvider(kClass)) {
            true
        } else {
            parentContainers.firstOrNull { it.canProvide(kClass) } != null
        }
    }

    override fun <T> provide(kClass: KClass<*>): T {
        assert(canProvide(kClass)) { "Cannot provide class ${kClass.qualifiedName}" }

        val provider = providerMap[findProviderClass(kClass)]
        return if (provider != null) {
            @Suppress("UNCHECKED_CAST")
            provider.provide() as T
        } else {
            parentContainers.firstOrNull { it.canProvide(kClass) }?.provide(kClass)
                ?: throw IllegalArgumentException("No provider found for class ${kClass.simpleName}")
        }
    }

    private fun hasProvider(kClass: KClass<*>) = findProviderClass(kClass) != null

    private fun findProviderClass(kClass: KClass<*>): KClass<*>? {
        return providerMap.keys.firstOrNull { kClass.isSubclassOf(it) }
    }
}
