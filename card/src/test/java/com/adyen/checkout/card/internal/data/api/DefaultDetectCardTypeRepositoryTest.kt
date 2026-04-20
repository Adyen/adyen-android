/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/11/2025.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.BinLookupCacheResult
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.cse.internal.TestCardEncryptor
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultDetectCardTypeRepositoryTest(
    @Mock private val binLookupService: BinLookupService,
) {

    private lateinit var networkCardBrandDetectionService: NetworkCardBrandDetectionService
    private lateinit var localCardBrandDetectionService: LocalCardBrandDetectionService
    private lateinit var binLookupCache: BinLookupCache
    private lateinit var detectCardTypeRepository: DefaultDetectCardTypeRepository

    @BeforeEach
    fun before() {
        initializeTest()
    }

    @Test
    fun `when detecting card types and the card number is shorter than bin size, then card brands are detected locally`() =
        runTest {
            val cardNumber = "6767"
            val flow = detectCardTypeRepository.detectCardTypes(cardNumber).test(testScheduler)

            // assert flow emits only once (local)
            assertEquals(1, flow.values.size)
            assertEquals(localCardBrandDetectionService.getCardBrands(cardNumber), flow.values[0])
        }

    @Nested
    @DisplayName("when detecting card types and the card number is equal or longer than bin size")
    inner class BinLookupTest {

        @Test
        fun `then local and network card types are emitted`() = runTest {
            whenever(binLookupService.makeBinLookup(any())).doReturn(mockBinLookupResponse())

            val cardNumber = "545454545454"
            val flow = detectCardTypeRepository.detectCardTypes(cardNumber).test(testScheduler)

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            // assert flow emits twice (local + network)
            assertEquals(2, flow.values.size)
            assertEquals(localCardBrandDetectionService.getCardBrands(cardNumber), flow.values[0])
            assertEquals(networkCardBrandDetectionService.getCardBrands(bin).getOrNull(), flow.values[1])
        }

        @Test
        fun `and the same BIN was already fetched, then cached card brands are emitted for second call`() =
            runTest {
                whenever(binLookupService.makeBinLookup(any())).doReturn(mockBinLookupResponse())

                detectCardTypeRepository.detectCardTypes("54545454545").test(testScheduler)

                val cardNumberWithSameBin = "545454545454"
                val secondFlow = detectCardTypeRepository
                    .detectCardTypes(cardNumberWithSameBin)
                    .test(testScheduler)

                // assert flow emits once (cache)
                assertEquals(1, secondFlow.values.size)

                val bin = detectCardTypeRepository.getBin(cardNumberWithSameBin)
                assertNotNull(bin)

                val cachedResult = binLookupCache.getResult(bin)
                assertInstanceOf<BinLookupCacheResult.Available>(cachedResult)

                assertEquals(cachedResult.detectedCardTypes, secondFlow.values[0])
            }

        @Test
        fun `and the same BIN is currently being fetched, then the second call should not emit any results`() =
            runTest {
                whenever(binLookupService.makeBinLookup(any())).doSuspendableAnswer {
                    // small delay to emulate a network request
                    delay(100)
                    mockBinLookupResponse()
                }

                detectCardTypeRepository.detectCardTypes("54545454545").test(testScheduler)

                val cardNumberWithSameBin = "545454545454"
                val secondFlow = detectCardTypeRepository
                    .detectCardTypes(cardNumberWithSameBin)
                    .test(testScheduler)

                advanceUntilIdle()

                // assert flow does not emit
                assert(secondFlow.values.isEmpty())
            }

        @Test
        fun `and a different BIN was already fetched, then network brands are emitted for second call`() = runTest {
            whenever(binLookupService.makeBinLookup(any())).doReturn(mockBinLookupResponse())

            detectCardTypeRepository.detectCardTypes("54545454545").test(testScheduler)

            val cardNumberWithDifferentBin = "411111111111"
            val secondFlow = detectCardTypeRepository
                .detectCardTypes(cardNumberWithDifferentBin)
                .test(testScheduler)

            val bin = detectCardTypeRepository.getBin(cardNumberWithDifferentBin)
            assertNotNull(bin)

            // assert flow emits twice (local + network)
            assertEquals(2, secondFlow.values.size)
            assertEquals(localCardBrandDetectionService.getCardBrands(cardNumberWithDifferentBin), secondFlow.values[0])
            assertEquals(networkCardBrandDetectionService.getCardBrands(bin).getOrNull(), secondFlow.values[1])
        }

        @Test
        fun `and a different BIN is currently being fetched, then network brands are emitted for second call`() =
            runTest {
                whenever(binLookupService.makeBinLookup(any())).doSuspendableAnswer {
                    // small delay to emulate a network request
                    delay(100)
                    mockBinLookupResponse()
                }

                detectCardTypeRepository.detectCardTypes("54545454545").test(testScheduler)

                val cardNumberWithDifferentBin = "411111111111"
                val secondFlow = detectCardTypeRepository
                    .detectCardTypes(cardNumberWithDifferentBin)
                    .test(testScheduler)

                advanceUntilIdle()

                val bin = detectCardTypeRepository.getBin(cardNumberWithDifferentBin)
                assertNotNull(bin)

                // assert flow emits twice (local + network)
                assertEquals(2, secondFlow.values.size)
                assertEquals(
                    localCardBrandDetectionService.getCardBrands(cardNumberWithDifferentBin),
                    secondFlow.values[0],
                )
                assertEquals(networkCardBrandDetectionService.getCardBrands(bin).getOrNull(), secondFlow.values[1])
            }

        @Test
        fun `and network results are returned, then results should be in cache`() = runTest {
            whenever(binLookupService.makeBinLookup(any())).doReturn(mockBinLookupResponse())

            val cardNumber = "545454545454"
            val flow = detectCardTypeRepository.detectCardTypes(cardNumber).test(testScheduler)
            // first value emitted is local, second is network, which are the ones that get cached
            val networkCardTypes = flow.values[1]

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            val result = binLookupCache.getResult(bin)
            assertInstanceOf<BinLookupCacheResult.Available>(result)
            assertEquals(networkCardTypes, result.detectedCardTypes)
        }

        @Test
        fun `and network results are being fetched, then cache should be marked as fetching`() = runTest {
            whenever(binLookupService.makeBinLookup(any())).doSuspendableAnswer {
                // small delay to emulate a network request
                delay(100)
                mockBinLookupResponse()
            }

            val cardNumber = "545454545454"
            detectCardTypeRepository.detectCardTypes(cardNumber).test(testScheduler)

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            val result = binLookupCache.getResult(bin)
            assertInstanceOf<BinLookupCacheResult.Fetching>(result)
        }

        @Test
        fun `and the network call fails, then cache should be empty`() = runTest {
            whenever(binLookupService.makeBinLookup(any())).doAnswer {
                throw HttpError(0, "test bin lookup failed", null)
            }

            val cardNumber = "545454545454"
            detectCardTypeRepository.detectCardTypes(cardNumber).test(testScheduler)

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            val result = binLookupCache.getResult(bin)
            assertInstanceOf<BinLookupCacheResult.Unavailable>(result)
        }
    }

    private fun mockBinLookupResponse(): BinLookupResponse {
        return BinLookupResponse(
            brands = listOf(
                Brand(
                    brand = "mc",
                    enableLuhnCheck = true,
                    supported = true,
                    cvcPolicy = "required",
                    expiryDatePolicy = "required",
                    panLength = 16,
                    paymentMethodVariant = "scheme",
                    localizedBrand = "MasterCard",
                ),
            ),
        )
    }

    private fun initializeTest(supportedCardBrands: List<CardBrand> = emptyList()) {
        binLookupCache = BinLookupCache()
        localCardBrandDetectionService = LocalCardBrandDetectionService(supportedCardBrands)
        networkCardBrandDetectionService = NetworkCardBrandDetectionService(
            cardEncryptor = TestCardEncryptor(),
            binLookupService = binLookupService,
            publicKey = "",
            supportedCardBrands = supportedCardBrands,
            paymentMethodType = "",
        )
        detectCardTypeRepository = DefaultDetectCardTypeRepository(
            binLookupCache,
            localCardBrandDetectionService,
            networkCardBrandDetectionService,
        )
    }
}
