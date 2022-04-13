/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/4/2022.
 */

package com.adyen.checkout.core.api

class HttpException(
    val code: Int,
    override val message: String,
    val errorBody: ErrorResponseBody?,
) : RuntimeException(message)
