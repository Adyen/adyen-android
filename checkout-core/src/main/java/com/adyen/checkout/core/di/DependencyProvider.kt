/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/4/2022.
 */

package com.adyen.checkout.core.di

fun interface DependencyProvider<T> {
    fun provide(): T
}
