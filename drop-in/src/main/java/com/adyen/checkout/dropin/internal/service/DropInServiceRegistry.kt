/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/3/2026.
 */

package com.adyen.checkout.dropin.internal.service

import com.adyen.checkout.dropin.DropInService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

internal object DropInServiceRegistry {

    private val service = MutableStateFlow<DropInService?>(null)

    fun register(service: DropInService) {
        this.service.value = service
    }

    fun unregister() {
        service.value = null
    }

    suspend fun awaitService(): DropInService {
        return service.filterNotNull().first()
    }
}
