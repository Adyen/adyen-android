/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/4/2022.
 */

package com.adyen.checkout.example.ui.main

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.sessions.model.Session

internal sealed class MainNavigation {
    object Card : MainNavigation()
    data class DropIn(val paymentMethodsApiResponse: PaymentMethodsApiResponse) : MainNavigation()
    data class DropInWithSession(val session: Session) : MainNavigation()
    data class DropInWithCustomSession(val session: Session) : MainNavigation()
}
