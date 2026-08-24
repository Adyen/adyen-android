/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.dropin.internal.helper.BackStackPersister
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DropInNavigator(
    private val backStackPersister: BackStackPersister,
) {

    // Snapshot state, so both Compose and anything outside of composition observe the same list: the former by
    // reading it during composition, the latter through snapshotFlow.
    // Initialized with an empty key to make the preselected bottom sheet possible
    private val _backStack: SnapshotStateList<NavKey> = mutableStateListOf(EmptyNavKey)
    val backStack: List<NavKey> get() = _backStack

    private val _finishFlow = MutableStateFlow(false)
    val finishFlow = _finishFlow.asStateFlow()

    val didRestoreState: Boolean

    init {
        val restored = backStackPersister.restore()
        didRestoreState = restored != null
        if (didRestoreState) {
            _backStack.clear()
            _backStack.addAll(restored)
        }
    }

    fun navigateTo(key: NavKey) {
        _backStack.add(key)
        onBackStackChanged()
    }

    fun clearAndNavigateTo(key: NavKey) {
        _backStack.clear()
        _backStack.add(EmptyNavKey)
        _backStack.add(key)
        onBackStackChanged()
    }

    fun back() {
        _backStack.removeLastOrNull()

        if (_backStack.singleOrNull() == EmptyNavKey) {
            _finishFlow.tryEmit(true)
        }
        onBackStackChanged()
    }

    /**
     * Ends Drop-in from anywhere on the back stack, unlike [back], which only finishes once the last key is popped.
     */
    fun finish() {
        _finishFlow.tryEmit(true)
    }

    fun isEmptyAfterCurrent(): Boolean {
        return _backStack.filterNot { it is EmptyNavKey }.size <= 1
    }

    private fun onBackStackChanged() {
        backStackPersister.store(_backStack)
    }
}
