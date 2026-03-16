/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/3/2026.
 */

package com.adyen.checkout.dropin.internal.service

import com.adyen.checkout.dropin.DropInService

internal object DropInServiceRegistry {
    private var service: DropInService? = null

    fun register(service: DropInService) {
        this.service = service
    }

    fun unregister() {
        service = null
    }

    fun get(): DropInService? = service
}
