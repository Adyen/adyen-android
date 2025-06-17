/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/1/2023.
 */

package com.adyen.checkout.sessions.core.internal.data.model

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionSetupConfiguration
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
