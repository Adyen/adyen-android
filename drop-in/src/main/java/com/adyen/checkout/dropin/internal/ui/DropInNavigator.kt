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

internal class DropInNavigator(
    private val backStackPersister: BackStackPersister,
) {

    val backStack: List<NavKey>
        field: SnapshotStateList<NavKey> = mutableStateListOf()

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
        backStack.add(key)
        persist()
    }

    /**
     * Going back from the last key empties the back stack, which is what dismisses Drop-in.
     */
    fun back() {
        backStack.removeLastOrNull()
        persist()
    }

    /**
     * Empties the back stack, which dismisses Drop-in.
     */
    fun clear() {
        backStack.clear()
        persist()
    }

    fun isEmptyAfterCurrent(): Boolean {
        return backStack.size <= 1
    }

    fun keyBelowCurrent(): NavKey? {
        return backStack.getOrNull(backStack.lastIndex - 1)
    }

    private fun persist() {
        backStackPersister.store(backStack)
    }
}
