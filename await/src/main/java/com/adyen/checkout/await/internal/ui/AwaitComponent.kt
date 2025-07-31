/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.await.internal.ui.view.AwaitComponent
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class AwaitComponent : ActionComponent {

    private val eventChannel = bufferedChannel<ActionComponentEvent>()
    override val eventFlow: Flow<ActionComponentEvent> = eventChannel.receiveAsFlow()

    override fun handleAction(context: Context) {
        // TODO - Implement handleAction
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        AwaitComponent(modifier = modifier)
    }
}
