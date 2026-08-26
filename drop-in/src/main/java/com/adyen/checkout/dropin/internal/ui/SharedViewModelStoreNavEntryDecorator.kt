/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2026.
 *
 * Adapted from the official Navigation 3 shared view model recipe, Copyright 2025 The Android Open Source Project,
 * licensed under the Apache License, Version 2.0. Keep this file in sync with:
 * https://github.com/android/nav3-recipes/blob/main/app/src/main/java/com/example/nav3recipes/sharedviewmodel/SharedViewModelStoreNavEntryDecorator.kt
 *
 * Deviations from the recipe, all of them deliberate:
 *  - declarations are internal rather than public, since this is not part of the SDK's public API.
 *  - the recipe's `toContentKey` helper is omitted. It reproduces nav3's default content key derivation so that a
 *    parent can be named by its key, which silently breaks if that default ever changes. Entries that take part in
 *    the sharing set an explicit [NavEntry.contentKey] instead, and pass that same value to [parent].
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * Returns a [SharedViewModelStoreNavEntryDecorator] that is remembered across recompositions.
 *
 * @param viewModelStoreOwner The [ViewModelStoreOwner] that provides the [ViewModelStore] to nav entries.
 */
@Composable
internal fun <T : Any> rememberSharedViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): SharedViewModelStoreNavEntryDecorator<T> {
    val viewModelStoreProvider = rememberViewModelStoreProvider(viewModelStoreOwner)
    return remember(viewModelStoreOwner) {
        SharedViewModelStoreNavEntryDecorator(viewModelStoreProvider)
    }
}

/**
 * Provides the content of a [NavEntry] with a new [ViewModelStoreOwner] and provides that [ViewModelStoreOwner] as a
 * [LocalViewModelStoreOwner] so that it is available within the content.
 *
 * If the [NavEntry] declares a parent through [parent], the parent's [ViewModelStoreOwner] is supplied as well through
 * [LocalSharedViewModelStoreOwner]. That lets an entry read both its own view models and its parent's, which is how a
 * payment flow continues on the controller the screen before it created.
 *
 * A parent outlives the entry that declared it even once the parent itself is gone: [ViewModelStoreProvider] counts the
 * entries referencing a store and [ViewModelStoreProvider.clearKey] only clears one after the last of them has been
 * released. Drop-in relies on that, because the action screen replaces the back stack and so pops the very entry it
 * takes its controller from.
 *
 * This requires the usage of [androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator] to ensure that the
 * [NavEntry] scoped [ViewModel]s can properly provide access to [androidx.lifecycle.SavedStateHandle]s.
 *
 * @param viewModelStoreProvider The [ViewModelStoreProvider] scoped to the parent [ViewModelStoreOwner].
 */
internal class SharedViewModelStoreNavEntryDecorator<T : Any>(
    viewModelStoreProvider: ViewModelStoreProvider,
) : NavEntryDecorator<T>(
    onPop = { key -> viewModelStoreProvider.clearKey(key) },
    decorate = { entry ->
        val localContentKey = entry.contentKey
        val localOwner = rememberViewModelStoreOwner(
            localContentKey,
            viewModelStoreProvider,
            savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
        )

        val localValues: MutableList<ProvidedValue<*>> = mutableListOf(
            LocalViewModelStoreOwner provides localOwner,
        )

        // If the entry declares a parent, also provide its parent's ViewModelStore
        val parentContentKey = entry.metadata[ParentKey]
        if (parentContentKey != null) {
            val parentOwner = rememberViewModelStoreOwner(
                parentContentKey,
                viewModelStoreProvider,
                savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
            )

            localValues.add(LocalSharedViewModelStoreOwner provides parentOwner)
        }

        CompositionLocalProvider(values = localValues.toTypedArray()) { entry.Content() }
    },
) {

    companion object {

        /**
         * Declares the entry's parent. The parent's [ViewModelStoreOwner] is supplied through
         * [LocalSharedViewModelStoreOwner].
         *
         * @param key The [NavEntry.contentKey] of the parent. The parent has to declare that same value as its own
         * [NavEntry.contentKey], rather than leaving it to nav3's default.
         */
        fun parent(key: Any) = metadata { put(ParentKey, key) }

        object ParentKey : NavMetadataKey<Any>
    }
}

internal val LocalSharedViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
    error("No LocalSharedViewModelStoreOwner provided!")
}
