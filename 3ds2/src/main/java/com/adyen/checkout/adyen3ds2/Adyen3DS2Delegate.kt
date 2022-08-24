/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/8/2022.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface Adyen3DS2Delegate {

    val detailsFlow: Flow<ActionComponentData>

    val exceptionFlow: Flow<CheckoutException>

    var uiCustomization: UiCustomization?

    fun initialize(coroutineScope: CoroutineScope)

    fun handleAction(action: Action, activity: Activity)

    fun handleIntent(intent: Intent)

    fun onCleared()
}
