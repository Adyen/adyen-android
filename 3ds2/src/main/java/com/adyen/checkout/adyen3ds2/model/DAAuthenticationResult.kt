/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 8/12/2022.
 */

package com.adyen.checkout.adyen3ds2.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class DAAuthenticationResult {
    class AuthenticationSuccessful(val sdkOutput: String) : DAAuthenticationResult()
    object NotNow : DAAuthenticationResult()
    object RemoveCredentials : DAAuthenticationResult()
    object Timeout : DAAuthenticationResult()
}
