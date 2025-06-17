/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */
package com.adyen.checkout.cse

import com.adyen.checkout.core.old.exception.CheckoutException

/**
 * Exception thrown when the Adyen encryption fails.
 */
class EncryptionException(message: String, cause: Throwable?) : CheckoutException(message, cause)
