/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.model

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.SessionSetupConfiguration
import kotlinx.parcelize.Parcelize

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionDetails(
    val id: String,
    val sessionData: String,
    val amount: Amount?,
    val expiresAt: String,
    val returnUrl: String?,
    val sessionSetupConfiguration: SessionSetupConfiguration?,
    val shopperLocale: String?,
    val environment: Environment,
    val clientKey: String,
) : Parcelable

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun CheckoutSession.mapToDetails(): SessionDetails {
    return SessionDetails(
        environment = environment,
        clientKey = clientKey,
        id = sessionSetupResponse.id,
        sessionData = sessionSetupResponse.sessionData,
        amount = sessionSetupResponse.amount,
        expiresAt = sessionSetupResponse.expiresAt,
        returnUrl = sessionSetupResponse.returnUrl,
        sessionSetupConfiguration = sessionSetupResponse.configuration,
        shopperLocale = sessionSetupResponse.shopperLocale,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun SessionDetails.mapToModel(): SessionModel {
    return SessionModel(
        id = id,
        sessionData = sessionData,
    )
}
