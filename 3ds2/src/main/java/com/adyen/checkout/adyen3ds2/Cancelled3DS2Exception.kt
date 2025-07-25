/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 15/1/2020.
 */
package com.adyen.checkout.adyen3ds2

import com.adyen.checkout.core.old.exception.ComponentException

/**
 * This exception is an indication that the 3DS2 Authentication was cancelled by the user.
 */
@Deprecated("This exception is no longer in use")
class Cancelled3DS2Exception(errorMessage: String) : ComponentException(errorMessage) {
    companion object {
        private const val serialVersionUID = 3858008275644429050L
    }
}
