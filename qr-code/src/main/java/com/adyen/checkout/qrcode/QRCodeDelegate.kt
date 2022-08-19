/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/8/2022.
 */

package com.adyen.checkout.qrcode

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface QRCodeDelegate {

    val outputDataFlow: Flow<QRCodeOutputData?>

    val outputData: QRCodeOutputData?

    val exceptionFlow: Flow<CheckoutException>

    val detailsFlow: Flow<JSONObject>

    val timerFlow: Flow<TimerData>

    fun initialize(coroutineScope: CoroutineScope)

    fun handleAction(action: QrCodeAction, activity: Activity, paymentData: String)

    fun refreshStatus(paymentData: String)

    fun handleIntent(intent: Intent)

    fun onCleared()
}
