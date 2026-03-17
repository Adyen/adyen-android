/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DropInNavigator(
    private val savedStateHandle: SavedStateHandle,
) {

    // Initialized with an empty key to make the preselected bottom sheet possible
    private val _backStack: SnapshotStateList<NavKey> = mutableStateListOf(EmptyNavKey)
    val backStack: List<NavKey> get() = _backStack

    private val _finishFlow = MutableStateFlow(false)
    val finishFlow = _finishFlow.asStateFlow()

    private val serializer = SnapshotStateListSerializer(elementSerializer = NavKeySerializer())

    init {
        val saved = savedStateHandle.get<Bundle>(BACK_STACK_KEY)
        if (saved != null) {
            val restored = decodeFromSavedState(serializer, saved)
            _backStack.clear()
            _backStack.addAll(restored)
        }
    }

    fun navigateTo(key: NavKey) {
        _backStack.add(key)
        persist()
    }

    fun clearAndNavigateTo(key: NavKey) {
        _backStack.clear()
        _backStack.add(EmptyNavKey)
        _backStack.add(key)
        persist()
    }

    fun back() {
        _backStack.removeLastOrNull()

        if (_backStack.singleOrNull() == EmptyNavKey) {
            _finishFlow.tryEmit(true)
        }
        persist()
    }

    fun isEmptyAfterCurrent(): Boolean {
        return _backStack.filterNot { it is EmptyNavKey }.size <= 1
    }

    private fun persist() {
        val saved = encodeToSavedState(serializer, _backStack)
        savedStateHandle[BACK_STACK_KEY] = saved
    }

    companion object {
        private const val BACK_STACK_KEY = "DROP_IN_BACK_STACK"
    }
}
