/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import android.os.Parcelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.parcelize.Parcelize

/**
 * The components whose secondary screens share one back stack: the component the host shows, followed by its
 * [SecondaryScreenComponent.childScreenComponents].
 */
internal class SecondaryScreenOwners(component: SecondaryScreenComponent) {

    private val owners = listOf(component) + component.childScreenComponents

    /**
     * The navigation events of every owner, each indexed by the owner that sent it.
     */
    val navigation: Flow<IndexedValue<SecondaryNavigationEvent>> = owners
        .mapIndexed { index, owner -> owner.navigation.map { event -> IndexedValue(index, event) } }
        .merge()

    /**
     * The component that opened [entry], which is also the one that renders it.
     */
    operator fun get(entry: SecondaryScreenEntry): SecondaryScreenComponent = owners[entry.ownerIndex]
}

/**
 * A screen on the back stack, and the index of the owner that opened it.
 */
@Parcelize
internal data class SecondaryScreenEntry(
    val key: String,
    val ownerIndex: Int,
) : Parcelable

/**
 * Opens a screen on top, owned by the owner that sent [event], or closes the top screen, whoever owns it.
 */
internal fun List<SecondaryScreenEntry>.navigate(
    event: IndexedValue<SecondaryNavigationEvent>,
): List<SecondaryScreenEntry> {
    return when (val navigationEvent = event.value) {
        is SecondaryNavigationEvent.Open -> this + SecondaryScreenEntry(navigationEvent.key, event.index)
        SecondaryNavigationEvent.Close -> dropLast(1)
    }
}
