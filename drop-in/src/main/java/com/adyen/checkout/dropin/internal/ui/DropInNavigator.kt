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

    // Initialized with an empty key to make the preselected bottom sheet possible
    val backStack: List<NavKey>
        field: SnapshotStateList<NavKey> = mutableStateListOf(EmptyNavKey)

    private val _finishFlow = MutableStateFlow(false)
    val finishFlow = _finishFlow.asStateFlow()

    val didRestoreState: Boolean

    init {
        val restored = backStackPersister.restore()
        didRestoreState = restored != null
        if (didRestoreState) {
            backStack.clear()
            backStack.addAll(restored)
        }
    }

    fun navigateTo(key: NavKey) {
        backStack.add(key)
        persist()
    }

    fun clearAndNavigateTo(key: NavKey) {
        backStack.clear()
        backStack.add(EmptyNavKey)
        backStack.add(key)
        persist()
    }

    fun back() {
        backStack.removeLastOrNull()

        if (backStack.singleOrNull() == EmptyNavKey) {
            _finishFlow.tryEmit(true)
        }
        persist()
    }

    fun backTo(key: NavKey) {
        if (backStack.lastOrNull() == key || key !in backStack) return

        while (backStack.lastOrNull() != key) {
            backStack.removeLastOrNull()
        }
        persist()
    }

    fun isEmptyAfterCurrent(): Boolean {
        return backStack.filterNot { it is EmptyNavKey }.size <= 1
    }

    private fun persist() {
        backStackPersister.store(backStack)
    }
}
