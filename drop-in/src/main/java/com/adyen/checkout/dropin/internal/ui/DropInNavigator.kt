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

internal class DropInNavigator {

    private val _backStack: SnapshotStateList<NavKey> = mutableStateListOf(EmptyNavKey)
    val backStack: List<NavKey> get() = _backStack

    fun navigateTo(key: NavKey) {
        _backStack.add(key)
    }

    fun clearAndNavigateTo(key: NavKey) {
        _backStack.removeIf { it != EmptyNavKey }
        _backStack.add(key)
    }

    fun back() {
        _backStack.removeLastOrNull()
    }
}
