/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.adyen3ds2.internal.data.api

import com.adyen.checkout.adyen3ds2.internal.data.model.SubmitFingerprintRequest
import com.adyen.checkout.adyen3ds2.internal.data.model.SubmitFingerprintResult
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.Threeds2Action
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.core.internal.util.runSuspendCatching
import org.json.JSONObject

internal class SubmitFingerprintRepository internal constructor(
    private val submitFingerprintService: SubmitFingerprintService
) {

    suspend fun submitFingerprint(
        encodedFingerprint: String,
        clientKey: String,
        paymentData: String?
    ): Result<SubmitFingerprintResult> = runSuspendCatching {
        Logger.d(TAG, "Submitting fingerprint automatically")

        val request = SubmitFingerprintRequest(
            encodedFingerprint = encodedFingerprint,
            paymentData = paymentData
        )

        val response = submitFingerprintService.submitFingerprint(request, clientKey)

        when {
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
                error("Failed to retrieve 3DS2 fingerprint result")
            }
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val RESPONSE_TYPE_COMPLETED = "completed"
        private const val RESPONSE_TYPE_ACTION = "action"
    }
}
