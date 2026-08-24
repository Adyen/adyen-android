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
    private val _backStack: SnapshotStateList<NavKey> = mutableStateListOf(EmptyNavKey)
    val backStack: List<NavKey> get() = _backStack

    val currentKey: NavKey? get() = _backStack.lastOrNull()

    private val _backStackFlow = MutableStateFlow<List<NavKey>>(_backStack.toList())
    val backStackFlow = _backStackFlow.asStateFlow()

    private val _finishFlow = MutableStateFlow(false)
    val finishFlow = _finishFlow.asStateFlow()

    val didRestoreState: Boolean

    init {
        val restored = backStackPersister.restore()?.let(::sanitizeRestored)
        // A back stack that only holds the empty key has nothing to display, the starting point is
        // determined as if there was no state to restore
        didRestoreState = restored != null && restored.size > 1
        if (didRestoreState) {
            _backStack.clear()
            _backStack.addAll(requireNotNull(restored))
            onBackStackChanged()
        }
    }

    /**
     * An action or secondary screen cannot be restored: its controller, and with it the action being
     * handled, does not survive process death. The payment method of the same flow is displayed instead.
     */
    private fun sanitizeRestored(restored: List<NavKey>): List<NavKey> = restored
        .map { key ->
            when (key) {
                is ActionNavKey -> PaymentMethodNavKey(key.paymentFlowType)
                is SecondaryNavKey -> PaymentMethodNavKey(key.paymentFlowType)
                else -> key
            }
        }
        .fold(mutableListOf<NavKey>()) { backStack, key ->
            if (backStack.lastOrNull() != key) backStack.add(key)
            backStack
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
     * Removes every key above [key], making it the displayed one.
     *
     * @return `false` when [key] is not on the back stack, leaving it unchanged.
     */
    fun popTo(key: NavKey): Boolean {
        val index = _backStack.indexOf(key)
        if (index == -1) return false

        while (_backStack.lastIndex > index) {
            _backStack.removeAt(_backStack.lastIndex)
        }
        onBackStackChanged()
        return true
    }

    fun isEmptyAfterCurrent(): Boolean {
        return _backStack.filterNot { it is EmptyNavKey }.size <= 1
    }

    private fun onBackStackChanged() {
        _backStackFlow.value = _backStack.toList()
        backStackPersister.store(_backStack)
    }
}
