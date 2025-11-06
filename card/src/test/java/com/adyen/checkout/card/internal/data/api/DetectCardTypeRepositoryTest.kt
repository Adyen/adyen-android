/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/11/2025.
 */

package com.adyen.checkout.card.internal.data.api

import app.cash.turbine.test
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.cse.internal.TestCardEncryptor
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
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

    @BeforeEach
    fun before() {
        cardEncryptor = TestCardEncryptor()
        detectCardTypeRepository = DefaultDetectCardTypeRepository(cardEncryptor, binLookupService)
    }

    @Nested
    @DisplayName("when card number is less than 11 digits ")
    inner class LocalDetectionTest {

        @Test
        fun `then the brand should be detected locally and isReliable should be false, enableLuhnCheck should be true, expiryDatePolicy should be required`() =
            runTest {
                detectCardTypeRepository.detectedCardTypesFlow.test {
                    val cardNumber = "6767"

                    val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                    detectCardTypeRepository.detectCardType(
                        cardNumber = cardNumber,
                        publicKey = "",
                        supportedCardBrands = listOf(CardBrand(CardType.SOLO.txVariant)),
                        clientKey = "",
                        coroutineScope = coroutineScope,
                        type = "",
                    )

                    val actual = expectMostRecentItem()
                    assertFalse(actual.first().isReliable)
                    assertTrue(actual.first().enableLuhnCheck)
                    assertEquals(Brand.FieldPolicy.REQUIRED, actual.first().expiryDatePolicy)
                }
            }

        @Test
        fun `and the brand is in supportedCardBrands, then emitted detected card type should be supported`() = runTest {
            detectCardTypeRepository.detectedCardTypesFlow.test {
                val cardNumber = "6767"

                val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                detectCardTypeRepository.detectCardType(
                    cardNumber = cardNumber,
                    publicKey = "",
                    supportedCardBrands = listOf(CardBrand(CardType.SOLO.txVariant)),
                    clientKey = "",
                    coroutineScope = coroutineScope,
                    type = "",
                )

                val detectedCardSolo = expectMostRecentItem().first {
                    it.cardBrand == CardBrand(CardType.SOLO.txVariant)
                }
                assertTrue(detectedCardSolo.isSupported)
            }
        }

        @Test
        fun `and the brand is not in supportedCardBrands, then emitted detected card type should not be supported`() =
            runTest {
                detectCardTypeRepository.detectedCardTypesFlow.test {
                    val cardNumber = "5454"

                    val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                    detectCardTypeRepository.detectCardType(
                        cardNumber = cardNumber,
                        publicKey = "",
                        supportedCardBrands = listOf(),
                        clientKey = "",
                        coroutineScope = coroutineScope,
                        type = "",
                    )

                    assertFalse(expectMostRecentItem().first().isSupported)
                }
            }

        @Test
        fun `and the brand is a no cvc brand, then emitted detected card type should have cvc as hidden`() = runTest {
            detectCardTypeRepository.detectedCardTypesFlow.test {
                val cardNumber = "6703"

                val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                detectCardTypeRepository.detectCardType(
                    cardNumber = cardNumber,
                    publicKey = "",
                    supportedCardBrands = listOf(),
                    clientKey = "",
                    coroutineScope = coroutineScope,
                    type = "",
                )

                assertEquals(Brand.FieldPolicy.HIDDEN, expectMostRecentItem().first().cvcPolicy)
            }
        }

        @Test
        fun `and the brand is not a no cvc brand, then emitted detected card type should have cvc as hidden`() =
            runTest {
                detectCardTypeRepository.detectedCardTypesFlow.test {
                    val cardNumber = "5454"

                    val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                    detectCardTypeRepository.detectCardType(
                        cardNumber = cardNumber,
                        publicKey = "",
                        supportedCardBrands = listOf(),
                        clientKey = "",
                        coroutineScope = coroutineScope,
                        type = "",
                    )

                    assertEquals(Brand.FieldPolicy.REQUIRED, expectMostRecentItem().first().cvcPolicy)
                }
            }
    }

    @Nested
    @DisplayName("when card number at least 11 digits")
    inner class BinLookupTest {

        @Test
        fun `and detectCardTypes is called for the first time with that bin, then a bin lookup call should be made`() =
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
                // Ignore locally detected items
                detectCardTypeRepository
                    .detectedCardTypesFlow
                    .filter { it.all { detectedCardType -> detectedCardType.isReliable } }
                    .test {
                        val cardNumber = "545454545454"
                        val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                        detectCardTypeRepository.detectCardType(
                            cardNumber = cardNumber,
                            publicKey = "",
                            supportedCardBrands = listOf(),
                            clientKey = "",
                            coroutineScope = coroutineScope,
                            type = "",
                        )
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

                        assertEquals(expected, expectMostRecentItem().first())
                    }
            }

        @Test
        fun `and detectCardTypes is called more than once with the same bin, only one bin lookup call should be made`() =
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

                // Ignore locally detected items
                detectCardTypeRepository
                    .detectedCardTypesFlow
                    .filter { it.all { detectedCardType -> detectedCardType.isReliable } }
                    .test {
                        val cardNumber = "54545454545"
                        val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
                        // First call result is cached
                        detectCardTypeRepository.detectCardType(
                            cardNumber = cardNumber,
                            publicKey = "",
                            supportedCardBrands = listOf(),
                            clientKey = "",
                            coroutineScope = coroutineScope,
                            type = "",
                        )
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
                        assertEquals(expected, expectMostRecentItem().first())

                        val cardNumber2 = "545454545454"
                        detectCardTypeRepository.detectCardType(
                            cardNumber = cardNumber2,
                            publicKey = "",
                            supportedCardBrands = listOf(),
                            clientKey = "",
                            coroutineScope = coroutineScope,
                            type = "",
                        )

                        verify(binLookupService, times(1)).makeBinLookup(any(), any())
                        assertEquals(expected, expectMostRecentItem().first())
                    }
            }
    }
}
