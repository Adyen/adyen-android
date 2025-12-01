/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2021.
 */

package com.adyen.checkout.threeds2.internal.data.api

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.data.Threeds2Action
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.threeds2.internal.data.model.SubmitFingerprintRequest
import com.adyen.checkout.threeds2.internal.data.model.SubmitFingerprintResult
import org.json.JSONObject

internal class SubmitFingerprintRepository(
    private val submitFingerprintService: SubmitFingerprintService
) {

    suspend fun submitFingerprint(
        encodedFingerprint: String,
        clientKey: String,
        paymentData: String?
    ): Result<SubmitFingerprintResult> = runSuspendCatching {
        adyenLog(AdyenLogLevel.DEBUG) { "Submitting fingerprint automatically" }

        val request = SubmitFingerprintRequest(
            encodedFingerprint = encodedFingerprint,
            paymentData = paymentData,
        )

        val response = submitFingerprintService.submitFingerprint(request, clientKey)

        when {
            response.type == RESPONSE_TYPE_COMPLETED && response.details != null -> {
                adyenLog(AdyenLogLevel.DEBUG) { "submitFingerprint: challenge completed" }
                SubmitFingerprintResult.Completed(JSONObject(response.details))
            }

            response.type == RESPONSE_TYPE_ACTION && response.action is RedirectAction -> {
                adyenLog(AdyenLogLevel.DEBUG) { "submitFingerprint: received new RedirectAction" }
                SubmitFingerprintResult.Redirect(response.action)
            }

            response.type == RESPONSE_TYPE_ACTION && response.action is Threeds2Action -> {
                adyenLog(AdyenLogLevel.DEBUG) { "submitFingerprint: received new Threeds2Action" }
                SubmitFingerprintResult.Threeds2(response.action)
            }

            else -> {
                adyenLog(AdyenLogLevel.DEBUG) { "submitFingerprint: unexpected response $response" }
                error("Failed to retrieve 3DS2 fingerprint result")
            }
        }
    }

    companion object {
        private const val RESPONSE_TYPE_COMPLETED = "completed"
        private const val RESPONSE_TYPE_ACTION = "action"
    }
}
