/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/10/2024.
 */

package com.adyen.checkout.components.core.internal.analytics

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent.Error.Type

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class ErrorEvent(val errorType: Type, val errorCode: String) {

    // Redirect
    REDIRECT_FAILED(Type.REDIRECT, "600"),
    REDIRECT_PARSE_FAILED(Type.REDIRECT, "601"),

    // Encryption
    ENCRYPTION(Type.INTERNAL, "610"),

    // Third party
    THIRD_PARTY(Type.THIRD_PARTY, "611"),

    // API
    API_PAYMENTS(Type.API_ERROR, "620"),
    API_PAYMENTS_DETAILS(Type.API_ERROR, "621"),
    API_THREEDS2(Type.API_ERROR, "622"),
    API_ORDER(Type.API_ERROR, "623"),
    API_PUBLIC_KEY(Type.API_ERROR, "624"),
    API_NATIVE_REDIRECT(Type.API_ERROR, "625"),

    // 3DS2
    THREEDS2_TOKEN_DECODING(Type.THREEDS2, "704"),
    THREEDS2_FINGERPRINT_CREATION(Type.THREEDS2, "705"),
    THREEDS2_TRANSACTION_CREATION(Type.THREEDS2, "706"),
    THREEDS2_TRANSACTION_MISSING(Type.THREEDS2, "707"),
    THREEDS2_FINGERPRINT_HANDLING(Type.THREEDS2, "708"),
    THREEDS2_CHALLENGE_HANDLING(Type.THREEDS2, "709"),
}
