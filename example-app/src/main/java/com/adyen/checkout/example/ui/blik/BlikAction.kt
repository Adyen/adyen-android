/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 21/11/2022.
 */

package com.adyen.checkout.example.ui.blik

import com.adyen.checkout.components.model.payments.response.Action

sealed class BlikAction {
    class Await(val action: Action) : BlikAction()
    object Unsupported : BlikAction()
}
