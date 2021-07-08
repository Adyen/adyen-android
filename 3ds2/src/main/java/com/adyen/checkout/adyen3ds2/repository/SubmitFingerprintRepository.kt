/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2.repository

import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.adyen3ds2.connection.SubmitFingerprintConnection
import com.adyen.checkout.adyen3ds2.model.SubmitFingerprintRequest
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONObject

class SubmitFingerprintRepository {

    companion object {
        private val TAG = LogUtil.getTag()
        private const val RESPONSE_TYPE_COMPLETED = "completed"
        private const val RESPONSE_TYPE_ACTION = "action"
    }

    suspend fun submitFingerprint(
        encodedFingerprint: String,
        configuration: Adyen3DS2Configuration,
        paymentData: String?
    ): SubmitFingerprintResult {
        Logger.d(TAG, "Submitting fingerprint automatically")
        val request = SubmitFingerprintRequest(
            encodedFingerprint = encodedFingerprint,
            paymentData = paymentData
        )
        val response = SubmitFingerprintConnection(
            request,
            environment = configuration.environment,
            clientKey = configuration.clientKey
        ).suspendedCall()
        return when {
            response.type == RESPONSE_TYPE_COMPLETED && response.details != null -> {
                Logger.d(TAG, "submitFingerprint: challenge completed")
                SubmitFingerprintResult.Completed(JSONObject(response.details))
            }
            response.type == RESPONSE_TYPE_ACTION && response.action is RedirectAction -> {
                Logger.d(TAG, "submitFingerprint: received new RedirectAction")
                SubmitFingerprintResult.Redirect(response.action)
            }
            response.type == RESPONSE_TYPE_ACTION && response.action is Threeds2Action -> {
                Logger.d(TAG, "submitFingerprint: received new Threeds2Action")
                SubmitFingerprintResult.Threeds2(response.action)
            }
            else -> {
                Logger.e(TAG, "submitFingerprint: unexpected response $response")
                throw ComponentException("Failed to retrieve 3DS2 fingerprint result")
            }
        }
    }
}
