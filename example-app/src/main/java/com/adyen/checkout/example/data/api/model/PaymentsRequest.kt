/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/3/2022.
 */

package com.adyen.checkout.example.data.api.model

import androidx.annotation.Keep
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.example.repositories.PaymentsRepositoryImpl
import org.json.JSONObject

/**
 * Data inside this class will not be sent as shown, instead paymentComponentData and requestData will
 * both be merged into the same JSON object. Check [PaymentsRepositoryImpl] for implementation.
 */
@Keep
data class PaymentsRequest(
    val paymentComponentData: JSONObject,
    val requestData: PaymentsRequestData
)

@Keep
data class PaymentsRequestData(
    val shopperReference: String,
    val amount: Amount?,
    val countryCode: String,
    val merchantAccount: String,
    val returnUrl: String,
    val additionalData: AdditionalData,
    val threeDSAuthenticationOnly: Boolean,
    val shopperIP: String,
    val reference: String,
    val channel: String,
    val lineItems: List<Item>,
    val shopperEmail: String? = null,
    val threeDS2RequestData: ThreeDS2RequestDataRequest?,
    val recurringProcessingModel: String?
)
