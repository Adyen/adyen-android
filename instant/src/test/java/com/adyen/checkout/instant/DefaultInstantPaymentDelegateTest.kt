/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/11/2022.
 */

package com.adyen.checkout.instant

import app.cash.turbine.test
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultInstantPaymentDelegateTest {

    private lateinit var delegate: DefaultInstantPaymentDelegate

    @BeforeEach
    fun before() {
        val configuration = InstantPaymentConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build()
        delegate = DefaultInstantPaymentDelegate(
            PaymentObserverRepository(),
            PaymentMethod(type = TYPE),
            GenericComponentParamsMapper(null).mapToParams(configuration)
        )
        Logger.setLogcatLevel(Logger.NONE)
    }

    @Test
    fun `when subscribed then component state flow should propagate a valid state`() = runTest {
        delegate.componentStateFlow.test {
            with(awaitItem()) {
                assertEquals(TYPE, data.paymentMethod?.type)
                assertTrue(isInputValid)
                assertTrue(isValid)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TYPE = "txVariant"
    }
}
