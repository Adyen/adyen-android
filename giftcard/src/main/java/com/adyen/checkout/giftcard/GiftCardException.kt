/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/3/2023.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.core.old.exception.CheckoutException

/**
 * Exception thrown when an error occurs during a payment flow using Gift Cards.
 */
class GiftCardException(errorMessage: String) : CheckoutException(errorMessage)
