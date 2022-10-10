/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/9/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.adyen3ds2.Adyen3DS2Delegate
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.base.StatusPollingDelegate
import com.adyen.checkout.components.base.ViewableDelegate
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.BaseThreeds2Action
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.qrcode.QRCodeOutputData
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class TestActionDelegate :
    ActionDelegate<Action>,
    DetailsEmittingDelegate,
    ViewableDelegate<OutputData>,
    IntentHandlingDelegate,
    StatusPollingDelegate,
    ViewProvidingDelegate {

    override val outputDataFlow: MutableStateFlow<QRCodeOutputData> = MutableStateFlow(
        QRCodeOutputData(
            isValid = false,
            paymentMethodType = null,
            qrCodeData = null
        )
    )

    override val outputData: QRCodeOutputData get() = outputDataFlow.value

    override val exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()

    override val detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSingleEventSharedFlow()

    override val timerFlow: MutableStateFlow<TimerData> = MutableStateFlow(TimerData(0, 0))

    override val viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)

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

    override fun getViewProvider(): ViewProvider {
        throw NotImplementedError()
    }
}

internal class Test3DS2Delegate : Adyen3DS2Delegate {

    override val detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSingleEventSharedFlow()

    override val exceptionFlow: Flow<CheckoutException> = MutableSingleEventSharedFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableSingleEventSharedFlow()

    var uiCustomization: UiCustomization? = null

    var handleActionCalled = false

    override fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        this.uiCustomization = uiCustomization
    }

    override fun handleAction(action: BaseThreeds2Action, activity: Activity) {
        handleActionCalled = true
    }

    override fun handleIntent(intent: Intent) = Unit

    override fun getViewProvider(): ViewProvider {
        throw IllegalStateException("This method should not be called from unit tests")
    }
}

internal object TestComponentViewType : ComponentViewType
