/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/7/2023.
 */

package com.adyen.checkout.components.core.internal.data.api

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupResponse
import com.adyen.checkout.components.core.internal.analytics.AnalyticsSource
import com.adyen.checkout.components.core.internal.analytics.data.old.AnalyticsMapper
import com.adyen.checkout.components.core.internal.analytics.data.old.AnalyticsRepositoryData
import com.adyen.checkout.components.core.internal.analytics.data.remote.AnalyticsService
import com.adyen.checkout.components.core.internal.analytics.data.old.DefaultOldAnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.exception.HttpException
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class DefaultOldAnalyticsRepositoryTest(
    @Mock private val analyticsService: AnalyticsService,
) {

    private val analyticsMapper: AnalyticsMapper = AnalyticsMapper()

    private lateinit var analyticsRepository: DefaultOldAnalyticsRepository

    @BeforeEach
    fun before() = runTest {
        analyticsRepository = getDefaultAnalyticsRepository()
        whenever(
            analyticsService.setupAnalytics(any(), any()),
        ) doReturn AnalyticsSetupResponse(checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID)
    }

    @Test
    fun `when repository is only instantiated then state is set as uninitialized`() = runTest {
        assertEquals(DefaultOldAnalyticsRepository.State.Uninitialized, analyticsRepository.state)
    }

    @Nested
    @DisplayName("when setupAnalytics is called")
    inner class InputDataChangedTest {

        @Test
        fun `then AnalyticsService is called`() = runTest {
            analyticsRepository.setupAnalytics()
            val analyticsSetupRequest = analyticsMapper.getAnalyticsSetupRequest(
                packageName = PACKAGE_NAME,
                locale = LOCALE,
                source = ANALYTICS_SOURCE,
                amount = TEST_AMOUNT,
                screenWidth = SCREEN_WIDTH.toLong(),
                paymentMethods = PAYMENT_METHODS,
                sessionId = TEST_SESSION_ID,
            )
            verify(analyticsService).setupAnalytics(analyticsSetupRequest, TEST_CLIENT_KEY)
        }

        @Test
        fun `and AnalyticsService is successful then state is set as initialized`() = runTest {
            analyticsRepository.setupAnalytics()
            assertEquals(DefaultOldAnalyticsRepository.State.Ready, analyticsRepository.state)
        }

        @Test
        fun `and AnalyticsService is successful then checkoutAttemptId is set`() = runTest {
            analyticsRepository.setupAnalytics()
            assertEquals(TEST_CHECKOUT_ATTEMPT_ID, analyticsRepository.getCheckoutAttemptId())
        }

        @Test
        fun `and AnalyticsService returns an error then state is set as error`() = runTest {
            whenever(analyticsService.setupAnalytics(any(), any())) doThrow HttpException(1, "error_message", null)
            analyticsRepository.setupAnalytics()
            assertEquals(DefaultOldAnalyticsRepository.State.Failed, analyticsRepository.state)
        }

        @Test
        fun `and AnalyticsService returns an error then checkoutAttemptId is not set`() = runTest {
            whenever(analyticsService.setupAnalytics(any(), any())) doThrow HttpException(1, "error_message", null)
            analyticsRepository.setupAnalytics()
            assertNull(analyticsRepository.getCheckoutAttemptId())
        }

        @Test
        fun `multiple times then AnalyticsService is only called once`() = runTest {
            analyticsRepository.setupAnalytics()
            analyticsRepository.setupAnalytics()
            analyticsRepository.setupAnalytics()
            verify(analyticsService, times(1)).setupAnalytics(any(), any())
        }

        @Test
        fun `multiple times and AnalyticsService returns an error then AnalyticsService is only called once`() =
            runTest {
                whenever(analyticsService.setupAnalytics(any(), any())) doThrow HttpException(1, "error_message", null)
                analyticsRepository.setupAnalytics()
                analyticsRepository.setupAnalytics()
                analyticsRepository.setupAnalytics()
                verify(analyticsService, times(1)).setupAnalytics(any(), any())
            }

        @Test
        fun `and level is set to ALL then call is made`() = runTest {
            analyticsRepository = getDefaultAnalyticsRepository(
                level = AnalyticsParamsLevel.ALL,
            )

            analyticsRepository.setupAnalytics()

            verify(analyticsService, times(1)).setupAnalytics(any(), any())
        }

        @Test
        fun `and level is set to NONE then call is not made`() = runTest {
            analyticsRepository = getDefaultAnalyticsRepository(
                level = AnalyticsParamsLevel.NONE,
            )

            analyticsRepository.setupAnalytics()

            verify(analyticsService, never()).setupAnalytics(any(), any())
        }

        @Test
        fun `and level is set to NONE then checkoutAttemptId is not set`() = runTest {
            analyticsRepository = getDefaultAnalyticsRepository(
                level = AnalyticsParamsLevel.NONE,
            )

            analyticsRepository.setupAnalytics()

            assertEquals(
                DefaultOldAnalyticsRepository.CHECKOUT_ATTEMPT_ID_FOR_DISABLED_ANALYTICS,
                analyticsRepository.getCheckoutAttemptId(),
            )
        }
    }

    @Suppress("LongParameterList")
    private fun getDefaultAnalyticsRepository(
        level: AnalyticsParamsLevel = AnalyticsParamsLevel.ALL,
        packageName: String = PACKAGE_NAME,
        locale: Locale = LOCALE,
        source: AnalyticsSource = ANALYTICS_SOURCE,
        analyticsService: AnalyticsService = this.analyticsService,
        analyticsMapper: AnalyticsMapper = this.analyticsMapper,
        clientKey: String = TEST_CLIENT_KEY,
        amount: Amount = TEST_AMOUNT,
        screenWidth: Int = SCREEN_WIDTH,
        paymentMethods: List<String> = PAYMENT_METHODS,
        sessionId: String? = TEST_SESSION_ID,
    ): DefaultOldAnalyticsRepository {
        return DefaultOldAnalyticsRepository(
            analyticsRepositoryData = AnalyticsRepositoryData(
                level = level,
                packageName = packageName,
                locale = locale,
                source = source,
                clientKey = clientKey,
                amount = amount,
                screenWidth = screenWidth,
                paymentMethods = paymentMethods,
                sessionId = sessionId,
            ),
            analyticsService = analyticsService,
            analyticsMapper = analyticsMapper,
        )
    }

    companion object {
        private const val PACKAGE_NAME = "com.adyen.checkout.test"
        private val LOCALE = Locale.US
        private val ANALYTICS_SOURCE = AnalyticsSource.DropIn
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val TEST_AMOUNT = Amount("USD", 1337)
        private const val SCREEN_WIDTH = 1080
        private val PAYMENT_METHODS = listOf("bcmc", "blik", "boletobancario")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"
        private const val TEST_SESSION_ID = "TEST_SESSION_ID"
    }
}
