/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/12/2020.
 */

package com.adyen.checkout.components.base.lifecycle

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner

@MainThread
inline fun <reified ViewModelT : ViewModel> viewModelFactory(crossinline factoryProducer: () -> ViewModelT) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return factoryProducer() as T
        }
    }

@MainThread
inline fun <reified ViewModelT : ViewModel> viewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    crossinline factoryProducer: (SavedStateHandle) -> ViewModelT
): AbstractSavedStateViewModelFactory {
    return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            @Suppress("UNCHECKED_CAST")
            return factoryProducer(handle) as T
        }
    }
}
