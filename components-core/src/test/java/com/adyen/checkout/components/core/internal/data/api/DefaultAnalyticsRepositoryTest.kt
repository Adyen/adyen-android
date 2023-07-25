/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/7/2023.
 */

package com.adyen.checkout.components.core.internal.data.api

import com.adyen.checkout.components.core.internal.data.model.AnalyticsSource
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.exception.HttpException
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultAnalyticsRepositoryTest(
    @Mock private val analyticsService: AnalyticsService,
) {

    private val analyticsMapper: AnalyticsMapper = AnalyticsMapper()

    private lateinit var analyticsRepository: DefaultAnalyticsRepository

    @BeforeEach
    fun before() {
        AdyenLogger.setLogLevel(Logger.NONE)

        analyticsRepository = getDefaultAnalyticsRepository()
    }

    @Test
    fun `when repository is only instantiated then state is set as uninitialized`() = runTest {
        assertEquals(DefaultAnalyticsRepository.State.Uninitialized, analyticsRepository.state)
    }

    @Nested
    @DisplayName("when setupAnalytics is called")
    inner class InputDataChangedTest {

        @Test
        fun `then AnalyticsService is called`() = runTest {
            analyticsRepository.setupAnalytics()
            val analyticsSetupRequest = analyticsMapper.getAnalyticsSetupRequest(PACKAGE_NAME, LOCALE, ANALYTICS_SOURCE)
            verify(analyticsService).setupAnalytics(analyticsSetupRequest, TEST_CLIENT_KEY)
        }

        @Test
        fun `and AnalyticsService is successful then state is set as initialized`() = runTest {
            analyticsRepository.setupAnalytics()
            assertEquals(DefaultAnalyticsRepository.State.Ready, analyticsRepository.state)
        }

        @Test
        fun `and AnalyticsService returns an error then state is set as error`() = runTest {
            whenever(analyticsService.setupAnalytics(any(), any())) doThrow HttpException(1, "error_message", null)
            analyticsRepository.setupAnalytics()
            assertEquals(DefaultAnalyticsRepository.State.Failed, analyticsRepository.state)
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
                level = AnalyticsParamsLevel.ALL
            )

            analyticsRepository.setupAnalytics()

            verify(analyticsService, times(1)).setupAnalytics(any(), any())
        }

        @Test
        fun `and level is set to NONE then call is not made`() = runTest {
            analyticsRepository = getDefaultAnalyticsRepository(
                level = AnalyticsParamsLevel.NONE
            )

            analyticsRepository.setupAnalytics()

            verify(analyticsService, never()).setupAnalytics(any(), any())
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
    ): DefaultAnalyticsRepository {
        return DefaultAnalyticsRepository(
            analyticsRepositoryData = AnalyticsRepositoryData(
                level = level,
                packageName = packageName,
                locale = locale,
                source = source,
                clientKey = clientKey,
            ),
            analyticsService = analyticsService,
            analyticsMapper = analyticsMapper,
        )
    }

    companion object {
        private const val PACKAGE_NAME = "com.adyen.checkout.test"
        private val LOCALE = Locale.US
        private val ANALYTICS_SOURCE = AnalyticsSource.DropIn()
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}