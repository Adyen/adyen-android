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
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.cse.internal.TestCardEncryptor
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DetectCardTypeRepositoryTest(
    @Mock private val binLookupService: BinLookupService,
) {

    private lateinit var cardEncryptor: TestCardEncryptor
    private lateinit var detectCardTypeRepository: DefaultDetectCardTypeRepository
    private lateinit var binLookupCache: BinLookupCache
    private lateinit var localCardBrandDetectionService: LocalCardBrandDetectionService
    private lateinit var networkCardBrandDetectionService: NetworkCardBrandDetectionService

    @BeforeEach
    fun before() {
        cardEncryptor = TestCardEncryptor()
        binLookupCache = BinLookupCache()
        localCardBrandDetectionService = LocalCardBrandDetectionService()
        networkCardBrandDetectionService = NetworkCardBrandDetectionService(
            cardEncryptor,
            binLookupService,
        )
        detectCardTypeRepository = DefaultDetectCardTypeRepository(
            binLookupCache,
            localCardBrandDetectionService,
            networkCardBrandDetectionService,
        )
    }

    @Nested
    @DisplayName("when detecting card types and the card number is shorter than bin size")
    inner class LocalDetectionTest {

        @Test
        fun `and the brand is in supportedCardBrands, then card brands are detected locally`() = runTest {
            val cardNumber = "6767"
            val cardBrand = CardBrand(CardType.SOLO.txVariant)

            val flow = detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = listOf(cardBrand),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)

            // assert flow emits only once
            assertEquals(1, flow.values.size)

            val detectedCardType = flow.latestValue.single { it.cardBrand == cardBrand }
            val expectedDetectedCardType = DetectedCardType(
                cardBrand = cardBrand,
                isReliable = false,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
                localizedBrand = null,
            )

            assertEquals(expectedDetectedCardType, detectedCardType)
        }

        @Test
        fun `and the brand is not in supportedCardBrands, then emitted detected card type should not be supported`() =
            runTest {
                val cardNumber = "6767"
                val cardBrand = CardBrand(CardType.SOLO.txVariant)

                val flow = detectCardTypeRepository.detectCardTypes(
                    cardNumber = cardNumber,
                    publicKey = "",
                    supportedCardBrands = listOf(CardBrand(CardType.MASTERCARD.txVariant)),
                    clientKey = "",
                    paymentMethodType = "",
                ).test(testScheduler)

                // assert flow emits only once
                assertEquals(1, flow.values.size)

                val detectedCardType = flow.latestValue.single { it.cardBrand == cardBrand }

                assertFalse(detectedCardType.isSupported)
            }

        @Test
        fun `and the brand is a no cvc brand, then emitted detected card type should have cvc as hidden`() = runTest {
            val cardNumber = "6703"
            val cardBrand = CardBrand(CardType.BCMC.txVariant)

            val flow = detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = listOf(cardBrand),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)

            // assert flow emits only once
            assertEquals(1, flow.values.size)

            val detectedCardType = flow.latestValue.single { it.cardBrand == cardBrand }

            assertEquals(Brand.FieldPolicy.HIDDEN, detectedCardType.cvcPolicy)
        }

        @Test
        fun `and the card number is empty, then an empty list should be emitted`() = runTest {
            val cardNumber = ""

            val flow = detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = emptyList(),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)

            // assert flow emits only once
            assertEquals(1, flow.values.size)

            assert(flow.latestValue.isEmpty())
        }
    }

    @Nested
    @DisplayName("when detecting card types and the card number is equal or longer than bin size")
    inner class BinLookupTest {

        @Test
        fun `then local and network card types are emitted and a bin lookup call is made`() = runTest {
            whenever(binLookupService.makeBinLookup(any(), any())).doReturn(
                BinLookupResponse(
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
                ),
            )

            val cardNumber = "545454545454"

            val flow = detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = listOf(),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)

            verify(binLookupService).makeBinLookup(any(), any())
            val expected = DetectedCardType(
                cardBrand = CardBrand(CardType.MASTERCARD.txVariant),
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = 16,
                paymentMethodVariant = PaymentMethodTypes.SCHEME,
                localizedBrand = "MasterCard",
            )

            // assert flow emits twice (local + network)
            assertEquals(2, flow.values.size)

            val localDetectedCardTypes = flow.values[0]
            assert(!localDetectedCardTypes.first().isReliable)

            val networkDetectedCardType = flow.values[1].first()
            assertEquals(expected, networkDetectedCardType)
        }

        @Test
        fun `and the same BIN was already was fetched, then only one bin lookup call should be made and results are returned`() =
            runTest {
                whenever(binLookupService.makeBinLookup(any(), any())).doReturn(
                    BinLookupResponse(
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
                    ),
                )

                val cardNumber = "54545454545"

                val firstFlow = detectCardTypeRepository.detectCardTypes(
                    cardNumber = cardNumber,
                    publicKey = "",
                    supportedCardBrands = listOf(),
                    clientKey = "",
                    paymentMethodType = "",
                ).test(testScheduler)

                // assert flow emits twice (local + network)
                assertEquals(2, firstFlow.values.size)
                assert(!firstFlow.values[0].first().isReliable)
                assert(firstFlow.values[1].first().isReliable)

                val cardNumberWithOneExtraDigit = "545454545454"
                val secondFlow = detectCardTypeRepository.detectCardTypes(
                    cardNumber = cardNumberWithOneExtraDigit,
                    publicKey = "",
                    supportedCardBrands = listOf(),
                    clientKey = "",
                    paymentMethodType = "",
                ).test(testScheduler)

                // assert flow emits once (cache)
                assertEquals(1, secondFlow.values.size)
                assert(secondFlow.values[0].first().isReliable)

                verify(binLookupService, times(1)).makeBinLookup(any(), any())
            }

        @Test
        fun `and the same BIN is currently being fetched, then only one bin lookup call should be made and second call does not return network results`() =
            runTest {
                whenever(binLookupService.makeBinLookup(any(), any())).doSuspendableAnswer {
                    // small delay to emulate a network request
                    delay(100)
                    BinLookupResponse(
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

                val cardNumber = "54545454545"
                val firstFlow = detectCardTypeRepository.detectCardTypes(
                    cardNumber = cardNumber,
                    publicKey = "",
                    supportedCardBrands = listOf(),
                    clientKey = "",
                    paymentMethodType = "",
                ).test(testScheduler)

                val cardNumberWithOneExtraDigit = "545454545454"
                val secondFlow = detectCardTypeRepository.detectCardTypes(
                    cardNumber = cardNumberWithOneExtraDigit,
                    publicKey = "",
                    supportedCardBrands = listOf(),
                    clientKey = "",
                    paymentMethodType = "",
                ).test(testScheduler)

                advanceUntilIdle()

                // assert flow emits twice (local + network)
                assertEquals(2, firstFlow.values.size)
                assert(!firstFlow.values[0].first().isReliable)
                assert(firstFlow.values[1].first().isReliable)

                // assert flow emits once (local)
                assertEquals(1, secondFlow.values.size)
                assert(!secondFlow.values[0].first().isReliable)
            }

        @Test
        fun `and results are returned, then results should be in cache`() = runTest {
            whenever(binLookupService.makeBinLookup(any(), any())).doReturn(
                BinLookupResponse(
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
                ),
            )

            val cardNumber = "545454545454"

            val flow = detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = listOf(),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)
            // first value emitted is local, second is network
            val detectedCardTypes = flow.values[1]

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            val result = binLookupCache.getResult(bin)
            assertInstanceOf<BinLookupCacheResult.Available>(result)
            assertEquals(detectedCardTypes, result.detectedCardTypes)
        }

        @Test
        fun `and results are being fetched, then cache should be marked as fetching`() = runTest {
            whenever(binLookupService.makeBinLookup(any(), any())).doSuspendableAnswer {
                // small delay to emulate a network request
                delay(100)
                BinLookupResponse(
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

            val cardNumber = "545454545454"

            detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = listOf(),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            val result = binLookupCache.getResult(bin)
            assertInstanceOf<BinLookupCacheResult.Fetching>(result)
        }

        @Test
        fun `and the call fails, then cache should be empty`() = runTest {
            whenever(binLookupService.makeBinLookup(any(), any())).doAnswer {
                throw HttpError(0, "test bin lookup failed", null)
            }

            val cardNumber = "545454545454"

            detectCardTypeRepository.detectCardTypes(
                cardNumber = cardNumber,
                publicKey = "",
                supportedCardBrands = listOf(),
                clientKey = "",
                paymentMethodType = "",
            ).test(testScheduler)

            val bin = detectCardTypeRepository.getBin(cardNumber)
            assertNotNull(bin)

            val result = binLookupCache.getResult(bin)
            assertInstanceOf<BinLookupCacheResult.Unavailable>(result)
        }
    }
}
