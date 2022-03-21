/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/8/2020.
 */
package com.adyen.checkout.components.util

/**
 * Helper class with a list of all the currently supported Actions on Components and Drop-In.
 */
object ActionTypes {
    const val AWAIT = "await"
    const val QR_CODE = "qrCode"
    const val REDIRECT = "redirect"
    const val SDK = "sdk"
    const val THREEDS2_CHALLENGE = "threeDS2Challenge"
    const val THREEDS2_FINGERPRINT = "threeDS2Fingerprint"
    const val THREEDS2 = "threeDS2"
    const val VOUCHER = "voucher"
}
