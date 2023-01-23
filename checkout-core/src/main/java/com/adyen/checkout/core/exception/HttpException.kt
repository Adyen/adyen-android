/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 10/1/2023.
 */

package com.adyen.checkout.core.exception

import com.adyen.checkout.core.api.ErrorResponseBody

class HttpException(
    val code: Int,
    override val message: String,
    val errorBody: ErrorResponseBody?,
) : CheckoutException(message)
