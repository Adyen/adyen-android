/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/8/2022.
 */

package com.adyen.checkout.adyen3ds2

sealed class Adyen3DS2Event {

    object CleanUp3DS2 : Adyen3DS2Event()

    object ClearPaymentData : Adyen3DS2Event()
}
