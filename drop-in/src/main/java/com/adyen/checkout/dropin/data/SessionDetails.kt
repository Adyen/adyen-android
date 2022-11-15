/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.dropin.data

import android.os.Parcelable
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.sessions.model.SessionModel
import com.adyen.checkout.sessions.model.setup.SessionSetupResponse
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SessionDetails(
    val id: String,
    val sessionData: String,
    val amount: Amount,
    val expiresAt: String,
    val returnUrl: String
) : Parcelable

internal fun SessionSetupResponse.mapToDetails(): SessionDetails {
    return SessionDetails(
        id = id,
        sessionData = sessionData,
        amount = amount ?: Amount.EMPTY,
        expiresAt = expiresAt,
        returnUrl = returnUrl,
    )
}

internal fun SessionDetails.mapToModel(): SessionModel {
    return SessionModel(
        id = id,
        sessionData = sessionData,
    )
}
