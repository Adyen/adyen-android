/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */
package com.adyen.checkout.cse.exception

import com.adyen.checkout.core.exception.CheckoutException

class EncryptionException(message: String, cause: Throwable?) : CheckoutException(message, cause) {
    companion object {
        private const val serialVersionUID = 604047691381396990L
    }
}
