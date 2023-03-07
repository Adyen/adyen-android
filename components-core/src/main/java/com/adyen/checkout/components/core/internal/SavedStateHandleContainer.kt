/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/11/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Implement this in any view model or class that contains a [SavedStateHandle].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SavedStateHandleContainer {
    val savedStateHandle: SavedStateHandle
}

/**
 * Use this class combined with a property to fetch data from a [SavedStateHandle] and save data to it.
 *
 * Property needs to be inside a [SavedStateHandleContainer].
 *
 * Example usage:
 *
 * ```
 * var amount: Amount? by SavedStateHandleProperty("amount_bundle_key")
 * ```
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SavedStateHandleProperty<T : Any>(private val key: String) : ReadWriteProperty<SavedStateHandleContainer, T?> {
    private var backingProperty: T? = null

    override fun getValue(thisRef: SavedStateHandleContainer, property: KProperty<*>): T? {
        if (backingProperty == null) {
            backingProperty = thisRef.savedStateHandle[key]
        }
        return backingProperty
    }

    override fun setValue(thisRef: SavedStateHandleContainer, property: KProperty<*>, value: T?) {
        thisRef.savedStateHandle[key] = value
        backingProperty = value
    }
}
