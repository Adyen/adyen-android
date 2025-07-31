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
import com.adyen.checkout.core.action.data.AwaitAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.data.api.StatusRepository
import com.adyen.checkout.core.components.internal.ui.StatusPollingComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("UnusedPrivateProperty", "LongParameterList")
internal class AwaitComponent(
    private val action: AwaitAction,
    private val coroutineScope: CoroutineScope,
    private val componentParams: ComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val redirectHandler: RedirectHandler,
    private val statusRepository: StatusRepository,
    private val paymentDataRepository: PaymentDataRepository,
) : ActionComponent, StatusPollingComponent {

    private val eventChannel = bufferedChannel<ActionComponentEvent>()
    override val eventFlow: Flow<ActionComponentEvent> = eventChannel.receiveAsFlow()

    override fun handleAction(context: Context) {
        // TODO - Implement handleAction
    }

    // TODO - Refresh status when user resumes the app
    override fun refreshStatus() {
        val paymentData = paymentDataRepository.paymentData ?: return
        statusRepository.refreshStatus(paymentData)
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        AwaitComponent(modifier = modifier)
    }
}
