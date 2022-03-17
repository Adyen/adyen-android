/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.exception

open class ComponentException : CheckoutException {
    constructor(errorMessage: String) : super(errorMessage)
    constructor(errorMessage: String, cause: Throwable?) : super(errorMessage, cause)
}
