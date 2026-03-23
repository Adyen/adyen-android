/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/3/2026.
 */

package com.adyen.checkout.dropin.internal.service

import com.adyen.checkout.dropin.DropInService
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class DropInServiceRegistryTest {

    @AfterEach
    fun tearDown() {
        DropInServiceRegistry.unregister()
    }

    @Test
    fun `when service is registered then awaitService returns it immediately`() = runTest {
        val service = mock<DropInService>()

        DropInServiceRegistry.register(service)

        val result = DropInServiceRegistry.awaitService()
        assertSame(service, result)
    }

    @Test
    fun `when service is registered after awaitService is called then awaitService resumes`() = runTest {
        val service = mock<DropInService>()
        val deferred = async { DropInServiceRegistry.awaitService() }

        DropInServiceRegistry.register(service)

        assertSame(service, deferred.await())
    }

    @Test
    fun `when service is re-registered after unregister then awaitService returns new service`() = runTest {
        val firstService = mock<DropInService>()
        DropInServiceRegistry.register(firstService)
        DropInServiceRegistry.unregister()

        val secondService = mock<DropInService>()
        val deferred = async { DropInServiceRegistry.awaitService() }

        DropInServiceRegistry.register(secondService)

        assertSame(secondService, deferred.await())
    }
}
