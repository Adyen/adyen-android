/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.adyen3ds2.ThreeDS2MainNavigationKey
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class ThreeDS2Component(
    private val threeDS2Delegate: ThreeDS2Delegate,
) : ActionComponent {

    override val eventFlow: Flow<ActionComponentEvent> = threeDS2Delegate.eventFlow

    private val threeDsEventChannel = bufferedChannel<ThreeDS2Event>()
    private val threeDsEventFlow: Flow<ThreeDS2Event> = threeDsEventChannel.receiveAsFlow()

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        Adyen3DS2NavKey to CheckoutNavEntry(Adyen3DS2NavKey, ThreeDS2MainNavigationKey) { _ -> MainScreen() },
    )

    override val navigationStartingPoint: NavKey = Adyen3DS2NavKey

    @Composable
    private fun MainScreen() {
        threeDsEvent(
            threeDS2Delegate = threeDS2Delegate,
            viewEventFlow = threeDsEventFlow,
            onError = threeDS2Delegate::emitError,
        )
    }

    override fun handleAction() {
        threeDsEventChannel.trySend(ThreeDS2Event.HandleAction)
    }
}
