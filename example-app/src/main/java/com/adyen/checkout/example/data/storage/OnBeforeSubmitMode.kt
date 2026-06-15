/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 15/6/2026.
 */

package com.adyen.checkout.example.data.storage

enum class OnBeforeSubmitMode {
    UPDATE_SHOPPER_DATA,
    PATCH_SESSION_AMOUNT,
    ABORT,
}