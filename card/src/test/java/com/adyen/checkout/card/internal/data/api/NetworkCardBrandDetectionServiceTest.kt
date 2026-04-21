/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/4/2026.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.BinLookupRequest
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.cse.internal.TestCardEncryptor
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class NetworkCardBrandDetectionServiceTest(
    @Mock private val binLookupService: BinLookupService,
) {

    private lateinit var cardEncryptor: TestCardEncryptor
    private lateinit var networkCardBrandDetectionService: NetworkCardBrandDetectionService

    @BeforeEach
    fun before() {
        initializeTest()
    }

    @Test
    fun `when getting card brands then bin lookup service is called`() = runTest {
        whenever(binLookupService.makeBinLookup(any()))
            .doReturn(BinLookupResponse(brands = listOf(mockBrand())))

        networkCardBrandDetectionService.getCardBrands("")

        verify(binLookupService, times(1)).makeBinLookup(any())
    }

    @Test
    fun `when public key is null then bin lookup is not called and error is returned`() = runTest {
        initializeTest(publicKey = null)
        val result = networkCardBrandDetectionService.getCardBrands("")

        assert(result.isFailure)
        assertEquals("Public key is missing.", result.exceptionOrNull()?.message)

        verify(binLookupService, never()).makeBinLookup(any())
    }

    @Test
    fun `when bin lookup is called the correct params are passed to it`() = runTest {
        whenever(binLookupService.makeBinLookup(any()))
            .doReturn(BinLookupResponse(brands = listOf(mockBrand())))

        val publicKey = "SOME_PUBLIC_KEY"
        val supportedCardBrands = listOf(CardBrand(CardType.VISA.txVariant), CardBrand("amex"))
        val paymentMethodType = "SOME_PAYMENT_METHOD_TYPE"
        initializeTest(publicKey, paymentMethodType, supportedCardBrands)

        val bin = "123412341234"
        networkCardBrandDetectionService.getCardBrands(bin)

        val binLookupRequestCaptor = argumentCaptor<BinLookupRequest>()
        verify(binLookupService, times(1)).makeBinLookup(binLookupRequestCaptor.capture())
        val binLookupRequest: BinLookupRequest = binLookupRequestCaptor.lastValue

        val expectedEncryptedBin = cardEncryptor.encryptBin(bin, publicKey)
        assertEquals(expectedEncryptedBin, binLookupRequest.encryptedBin)

        // validate requestId is a valid UUID
        assertEquals(UUID.fromString(binLookupRequest.requestId).toString(), binLookupRequest.requestId)

        val supportedBrands = listOf("visa", "amex")
        assertEquals(supportedBrands, binLookupRequest.supportedBrands)
        assertEquals(paymentMethodType, binLookupRequest.type)
    }

    @ParameterizedTest
    @MethodSource("mappingSource")
    fun `when getting card brands then response is mapped correctly`(
        binLookupBrands: List<Brand>?,
        expectedDetectedCardTypes: List<DetectedCardType>,
    ) = runTest {
        whenever(binLookupService.makeBinLookup(any()))
            .doReturn(BinLookupResponse(brands = binLookupBrands))

        val result = networkCardBrandDetectionService.getCardBrands("")

        val networkDetectedCardType = result.getOrNull()
        assertEquals(expectedDetectedCardTypes, networkDetectedCardType)
    }

    private fun initializeTest(
        publicKey: String? = "",
        paymentMethodType: String? = "",
        supportedCardBrands: List<CardBrand> = emptyList()
    ) {
        cardEncryptor = TestCardEncryptor()
        networkCardBrandDetectionService = NetworkCardBrandDetectionService(
            cardEncryptor = cardEncryptor,
            binLookupService = binLookupService,
            publicKey = publicKey,
            supportedCardBrands = supportedCardBrands,
            paymentMethodType = paymentMethodType,
        )
    }

    companion object {

        @JvmStatic
        fun mappingSource() = listOf(
            // binLookupBrands, expectedDetectedCardTypes
            arguments(
                listOf(mockBrand()),
                listOf(mockDetectedCardType()),
            ),
            arguments(
                listOf(mockBrand(brand = "visa")),
                listOf(mockDetectedCardType(cardBrand = CardBrand(CardType.VISA.txVariant))),
            ),
            arguments(
                listOf(mockBrand(brand = "unknown")),
                listOf(mockDetectedCardType(cardBrand = CardBrand("unknown"))),
            ),
            arguments(
                listOf(mockBrand(brand = null)),
                emptyList<DetectedCardType>(),
            ),
            arguments(
                listOf(mockBrand(enableLuhnCheck = false)),
                listOf(mockDetectedCardType(enableLuhnCheck = false)),
            ),
            arguments(
                listOf(mockBrand(enableLuhnCheck = null)),
                listOf(mockDetectedCardType(enableLuhnCheck = false)),
            ),
            arguments(
                listOf(mockBrand(supported = false)),
                listOf(mockDetectedCardType(isSupported = false)),
            ),
            arguments(
                listOf(mockBrand(supported = null)),
                listOf(mockDetectedCardType(isSupported = true)),
            ),
            arguments(
                listOf(mockBrand(cvcPolicy = "optional")),
                listOf(mockDetectedCardType(cvcPolicy = Brand.FieldPolicy.OPTIONAL)),
            ),
            arguments(
                listOf(mockBrand(cvcPolicy = "hidden")),
                listOf(mockDetectedCardType(cvcPolicy = Brand.FieldPolicy.HIDDEN)),
            ),
            arguments(
                listOf(mockBrand(cvcPolicy = null)),
                listOf(mockDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED)),
            ),
            arguments(
                listOf(mockBrand(expiryDatePolicy = "optional")),
                listOf(mockDetectedCardType(expiryDatePolicy = Brand.FieldPolicy.OPTIONAL)),
            ),
            arguments(
                listOf(mockBrand(expiryDatePolicy = "hidden")),
                listOf(mockDetectedCardType(expiryDatePolicy = Brand.FieldPolicy.HIDDEN)),
            ),
            arguments(
                listOf(mockBrand(expiryDatePolicy = null)),
                listOf(mockDetectedCardType(expiryDatePolicy = Brand.FieldPolicy.REQUIRED)),
            ),
            arguments(
                listOf(mockBrand(panLength = null, paymentMethodVariant = null, localizedBrand = null)),
                listOf(mockDetectedCardType(panLength = null, paymentMethodVariant = null, localizedBrand = null)),
            ),
            arguments(
                listOf(
                    mockBrand(),
                    mockBrand(brand = "visa"),
                    mockBrand(cvcPolicy = "hidden", expiryDatePolicy = "optional"),
                ),
                listOf(
                    mockDetectedCardType(),
                    mockDetectedCardType(cardBrand = CardBrand(CardType.VISA.txVariant)),
                    mockDetectedCardType(
                        cvcPolicy = Brand.FieldPolicy.HIDDEN,
                        expiryDatePolicy = Brand.FieldPolicy.OPTIONAL,
                    ),
                ),
            ),
            arguments(
                null,
                emptyList<DetectedCardType>(),
            ),
        )

        @Suppress("LongParameterList")
        private fun mockBrand(
            brand: String? = "mc",
            enableLuhnCheck: Boolean? = true,
            supported: Boolean? = true,
            cvcPolicy: String? = "required",
            expiryDatePolicy: String? = "required",
            panLength: Int? = 16,
            paymentMethodVariant: String? = "scheme",
            localizedBrand: String? = "MasterCard",
        ): Brand {
            return Brand(
                brand,
                enableLuhnCheck,
                supported,
                cvcPolicy,
                expiryDatePolicy,
                panLength,
                paymentMethodVariant,
                localizedBrand,
            )
        }

        @Suppress("LongParameterList")
        private fun mockDetectedCardType(
            cardBrand: CardBrand = CardBrand(CardType.MASTERCARD.txVariant),
            enableLuhnCheck: Boolean = true,
            cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
            isSupported: Boolean = true,
            panLength: Int? = 16,
            paymentMethodVariant: String? = PaymentMethodTypes.SCHEME,
            localizedBrand: String? = "MasterCard",
        ): DetectedCardType {
            return DetectedCardType(
                cardBrand,
                enableLuhnCheck,
                cvcPolicy,
                expiryDatePolicy,
                isSupported,
                panLength,
                paymentMethodVariant,
                localizedBrand,
            )
        }
    }
}
