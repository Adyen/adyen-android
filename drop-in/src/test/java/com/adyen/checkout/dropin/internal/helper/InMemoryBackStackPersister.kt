/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/3/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

internal class InMemoryBackStackPersister : BackStackPersister {

    private var backStack: SnapshotStateList<NavKey>? = null

    override fun store(backStack: SnapshotStateList<NavKey>) {
        this.backStack = backStack
    }

    override fun restore(): SnapshotStateList<NavKey>? {
        return backStack
    }
}
