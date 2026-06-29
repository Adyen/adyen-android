/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/3/2023.
 */

package com.adyen.checkout.authentication.internal.ui.model

internal data class AuthenticationComponentParams(
    val threeDSRequestorAppURL: String?,
    val deviceParameterBlockList: Set<String>?,
)
