/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */

package com.adyen.checkout.voucher

import android.app.Activity
import app.cash.turbine.test
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultVoucherDelegateTest {

    private lateinit var delegate: DefaultVoucherDelegate

    @BeforeEach
    fun beforeEach() {
        val configuration = VoucherConfiguration.Builder(Locale.getDefault(), Environment.TEST, TEST_CLIENT_KEY).build()
        delegate = DefaultVoucherDelegate(
            ActionObserverRepository(),
            GenericComponentParamsMapper().mapToParams(configuration),
        )
    }

    @Test
    fun `when handleAction called with valid data, then output data should be good`() = runTest {
        delegate.outputDataFlow.test {
            delegate.handleAction(
                VoucherAction(
                    paymentMethodType = "payment_method_type",
                    url = "download_url",
                    paymentData = "paymentData",
                ),
                Activity(),
            )

            skipItems(1)

            with(awaitItem()) {
                assertEquals("payment_method_type", paymentMethodType)
                assertEquals("download_url", downloadUrl)
            }
        }
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
