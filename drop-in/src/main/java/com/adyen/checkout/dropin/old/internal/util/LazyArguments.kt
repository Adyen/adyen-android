/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/9/2022.
 */

package com.adyen.checkout.dropin.old.internal.util

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.reflect.KProperty

internal inline fun <reified T> Activity.arguments(key: String): LazyProvider<Fragment, T> =
    argumentDelegate(key) { intent?.extras }

internal inline fun <reified T> Fragment.arguments(key: String): LazyProvider<Fragment, T> =
    argumentDelegate(key) { arguments }

internal inline fun <A, reified T> argumentDelegate(
    key: String,
    crossinline provideArguments: (A) -> Bundle?
): LazyProvider<A, T> = object : LazyProvider<A, T> {
    override fun provideDelegate(argumentsFactory: A, prop: KProperty<*>): Lazy<T> = lazy {
        provideArguments(argumentsFactory)?.get(key) as T
    }
}

internal interface LazyProvider<A, T> {
    operator fun provideDelegate(argumentsFactory: A, prop: KProperty<*>): Lazy<T>
}
