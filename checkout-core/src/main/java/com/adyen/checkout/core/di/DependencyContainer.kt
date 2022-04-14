/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/4/2022.
 */

package com.adyen.checkout.core.di

import kotlin.reflect.KClass

interface DependencyContainer {

    fun canProvide(kClass: KClass<*>): Boolean
    fun <T> provide(kClass: KClass<*>): T
}
