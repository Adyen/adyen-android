/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/8/2026.
 *
 * Adapted from androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecorator,
 * Copyright 2025 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

/**
 * Scopes view models to a flow of screens rather than to a single one.
 *
 * The nav3 decorator this replaces keys its view model store by [NavEntry.contentKey], so sharing a view model means
 * sharing a content key. That is not an option here: nav3 also treats one content key as one piece of content, both to
 * animate it and to render it in a single place, so two screens sharing a content key can never be visible at the same
 * time and the screen being navigated away from disappears instead of animating out.
 *
 * This decorator therefore reads its key from [scopeTo] metadata, leaving content keys unique. The store is cleared
 * once the last entry of a flow leaves the back stack, which cancels the view model scopes with it.
 */
@Composable
internal fun <T : Any> rememberFlowScopedViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): NavEntryDecorator<T> = remember(viewModelStoreOwner) {
    flowScopedViewModelStoreNavEntryDecorator(viewModelStoreOwner.viewModelStore)
}

/**
 * Declares that the entry shares its view models with every other entry of [flowKey].
 *
 * Entries without this metadata fall back to their content key, which scopes them to themselves.
 */
internal fun scopeTo(flowKey: Any): Map<String, Any> = mapOf(FLOW_KEY to flowKey)

private const val FLOW_KEY = "flow_scoped_view_models"

private fun <T : Any> flowScopedViewModelStoreNavEntryDecorator(
    hostViewModelStore: ViewModelStore,
): NavEntryDecorator<T> {
    val stores = hostViewModelStore.getFlowViewModelStores()

    return NavEntryDecorator(
        onPop = { contentKey -> stores.onEntryDisposed(contentKey) },
        decorate = { entry ->
            val flowViewModelStore = stores.storeFor(
                flowKey = entry.metadata[FLOW_KEY] ?: entry.contentKey,
                contentKey = entry.contentKey,
            )

            val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
            val entryViewModelStoreOwner = remember {
                FlowViewModelStoreOwner(flowViewModelStore, savedStateRegistryOwner)
            }

            CompositionLocalProvider(LocalViewModelStoreOwner provides entryViewModelStoreOwner) {
                entry.Content()
            }
        },
    )
}

/**
 * Holds one [ViewModelStore] per flow, alongside the entries currently keeping each of them alive.
 *
 * Kept in the host's [ViewModelStore] so the stores survive configuration changes, and so every flow is torn down with
 * the host.
 */
private class FlowViewModelStores : ViewModel() {

    private val stores = mutableMapOf<Any, ViewModelStore>()
    private val flowKeysByContentKey = mutableMapOf<Any, Any>()
    private val contentKeysByFlowKey = mutableMapOf<Any, MutableSet<Any>>()

    fun storeFor(flowKey: Any, contentKey: Any): ViewModelStore {
        flowKeysByContentKey[contentKey] = flowKey
        contentKeysByFlowKey.getOrPut(flowKey) { mutableSetOf() }.add(contentKey)
        return stores.getOrPut(flowKey) { ViewModelStore() }
    }

    /**
     * Clears the flow of [contentKey] once no entry of it is left.
     *
     * A flow outlives the entry that created it because the entry taking over is decorated before this runs:
     * [NavEntryDecorator] only reports an entry once its content has left composition, which is after the screen
     * replacing it has been composed.
     */
    fun onEntryDisposed(contentKey: Any) {
        val flowKey = flowKeysByContentKey.remove(contentKey) ?: return
        val contentKeys = contentKeysByFlowKey[flowKey] ?: return
        contentKeys.remove(contentKey)

        if (contentKeys.isEmpty()) {
            contentKeysByFlowKey.remove(flowKey)
            stores.remove(flowKey)?.clear()
        }
    }

    override fun onCleared() {
        stores.values.forEach { it.clear() }
        stores.clear()
        flowKeysByContentKey.clear()
        contentKeysByFlowKey.clear()
    }
}

private class FlowViewModelStoreOwner(
    override val viewModelStore: ViewModelStore,
    savedStateRegistryOwner: SavedStateRegistryOwner,
) : ViewModelStoreOwner,
    SavedStateRegistryOwner by savedStateRegistryOwner,
    HasDefaultViewModelProviderFactory {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = SavedStateViewModelFactory()

    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras().also {
            it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
            it[VIEW_MODEL_STORE_OWNER_KEY] = this
        }

    init {
        require(lifecycle.currentState == Lifecycle.State.INITIALIZED) {
            "The Lifecycle state is already beyond INITIALIZED. rememberFlowScopedViewModelStoreNavEntryDecorator " +
                "requires the SaveableStateHolderNavEntryDecorator to support SavedStateHandles."
        }
        enableSavedStateHandles()
    }
}

private fun ViewModelStore.getFlowViewModelStores(): FlowViewModelStores {
    val provider = ViewModelProvider.create(
        store = this,
        factory = viewModelFactory { initializer { FlowViewModelStores() } },
    )
    return provider[FlowViewModelStores::class]
}
