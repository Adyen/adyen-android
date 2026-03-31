/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/3/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import android.os.Bundle
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState

internal class SavedStateBackStackPersister(
    private val savedStateHandle: SavedStateHandle,
) : BackStackPersister {

    private val serializer = SnapshotStateListSerializer(elementSerializer = NavKeySerializer())

    override fun store(backStack: SnapshotStateList<NavKey>) {
        val savedState = encodeToSavedState(serializer, backStack)
        savedStateHandle[BACK_STACK_KEY] = savedState
    }

    override fun restore(): SnapshotStateList<NavKey>? {
        val saved = savedStateHandle.get<Bundle>(BACK_STACK_KEY) ?: return null
        return decodeFromSavedState(serializer, saved)
    }

    companion object {
        private const val BACK_STACK_KEY = "DROP_IN_BACK_STACK"
    }
}
