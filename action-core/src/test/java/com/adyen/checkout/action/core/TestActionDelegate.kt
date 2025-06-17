/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/9/2022.
 */

package com.adyen.checkout.action.core

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.components.core.internal.ui.ViewableDelegate
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.qrcode.internal.ui.model.QRCodeOutputData
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

internal class TestActionDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    ViewableDelegate<OutputData>,
    IntentHandlingDelegate,
    StatusPollingDelegate,
    ViewProvidingDelegate {

    override val outputDataFlow: MutableStateFlow<QRCodeOutputData> = MutableStateFlow(
        QRCodeOutputData(
            isValid = false,
            paymentMethodType = null,
            qrCodeData = null,
        ),
    )

    override val outputData: QRCodeOutputData get() = outputDataFlow.value

    override val exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSharedFlow(extraBufferCapacity = 1)

    override val detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSharedFlow(extraBufferCapacity = 1)

    override val timerFlow: MutableStateFlow<TimerData> = MutableStateFlow(TimerData(0, 0))

    override val viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)

    private val configuration = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "",
        amount = null,
        analyticsConfiguration = null,
    )
    override val componentParams: ComponentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
        .mapToParams(configuration, Locale.US, null, null)

    var initializeCalled = false
    override fun initialize(coroutineScope: CoroutineScope) {
        initializeCalled = true
    }

    var onClearedCalled = false
    override fun onCleared() {
        onClearedCalled = true
    }

    var handleActionCalled = false
    override fun handleAction(action: Action, activity: Activity) {
        handleActionCalled = true
    }

    var handleIntentCalled = false
    override fun handleIntent(intent: Intent) {
        handleIntentCalled = true
    }

    var refreshStatusCalled = false
    override fun refreshStatus() {
        refreshStatusCalled = true
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) = Unit

    override fun removeObserver() = Unit
}

internal class Test3DS2Delegate : Adyen3DS2Delegate {

    private val configuration: CheckoutConfiguration = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
    )

    override val componentParams: ComponentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
        .mapToParams(configuration, Locale.US, null, null)

    override val detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSharedFlow(extraBufferCapacity = 1)

    override val exceptionFlow: Flow<CheckoutException> = MutableSharedFlow(extraBufferCapacity = 1)

    override val viewFlow: Flow<ComponentViewType?> = MutableSharedFlow(extraBufferCapacity = 1)

    var handleActionCalled = false

    override fun initialize(coroutineScope: CoroutineScope) = Unit

    override fun handleAction(action: Action, activity: Activity) {
        handleActionCalled = true
    }

    override fun handleIntent(intent: Intent) = Unit

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) = Unit

    override fun removeObserver() = Unit

    override fun setOnRedirectListener(listener: () -> Unit) = Unit

    override fun onCleared() = Unit
}

private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
