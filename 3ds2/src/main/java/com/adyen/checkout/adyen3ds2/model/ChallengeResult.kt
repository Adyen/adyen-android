/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/5/2019.
 */
package com.adyen.checkout.adyen3ds2.model

import com.adyen.checkout.components.encoding.AndroidBase64Encoder
import com.adyen.threeds2.CompletionEvent
import org.json.JSONException
import org.json.JSONObject

class ChallengeResult private constructor(val isAuthenticated: Boolean, val payload: String) {

    companion object {
        private const val KEY_TRANSACTION_STATUS = "transStatus"
        private const val KEY_AUTHORISATION_TOKEN = "authorisationToken"
        private const val VALUE_TRANSACTION_STATUS = "Y"

        /**
         * Constructs the object base in the result from te 3DS2 SDK.
         *
         * @param completionEvent The result from the 3DS2 SDK.
         * @param authorisationToken The authorisationToken from the API.
         * @return The filled object with the content needed for the details response.
         * @throws JSONException In case parsing fails.
         */
        @Throws(JSONException::class)
        fun from(completionEvent: CompletionEvent, authorisationToken: String? = null): ChallengeResult {
            val transactionStatus = completionEvent.transactionStatus
            val isAuthenticated = VALUE_TRANSACTION_STATUS == transactionStatus
            val jsonObject = JSONObject()
            jsonObject.put(KEY_TRANSACTION_STATUS, transactionStatus)
            jsonObject.putOpt(KEY_AUTHORISATION_TOKEN, authorisationToken)
            val payload = AndroidBase64Encoder().encode(jsonObject.toString())
            return ChallengeResult(isAuthenticated, payload)
        }
    }
}
