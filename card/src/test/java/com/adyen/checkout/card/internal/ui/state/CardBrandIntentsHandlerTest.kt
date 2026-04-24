/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/4/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.card.internal.helper.DetectCardTypeBinHelper
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class CardBrandIntentsHandlerTest(
    @Mock private val cardComponentParams: CardComponentParams,
) {

    private val detectCardTypeBinHelper = DetectCardTypeBinHelper()
    private lateinit var cardBrandIntentsHandler: CardBrandIntentsHandler

    @BeforeEach
    fun beforeEach() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
        cardBrandIntentsHandler = CardBrandIntentsHandler(cardComponentParams, detectCardTypeBinHelper)
    }

    @Test
    fun `when intent is UpdateDetectedCardTypes and bin has changed already then intent should be discarded`() {
        val state = createInitialState().copy(cardNumber = TextInputComponentState(text = "5454545454545454"))
        val detectedCardTypes = listOf(
            createDetectedCardType().copy(cardBrand = CardBrand("visa")),
        )
        val detectedCardTypeList = DetectedCardTypeList(
            detectedCardTypes,
            DetectedCardTypeList.Source.NETWORK,
            "41111111111",
        )

        val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
            state,
            CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
        )

        assertEquals(state, actual)
    }

    @Nested
    @DisplayName("when intent is UpdateDetectedCardTypes and detection source is local")
    inner class UpdateDetectedCardTypesLocalTest {

        @Test
        fun `and list is empty then state should be no brands detected`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                emptyList(),
                DetectedCardTypeList.Source.LOCAL,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            assertEquals(CardBrandState.NoBrandsDetected, actual.cardBrandState)
        }

        @Test
        fun `and list has only unsupported brands then state should be no brands detected`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("visa"), isSupported = false),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.LOCAL,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            assertEquals(CardBrandState.NoBrandsDetected, actual.cardBrandState)
        }

        @Test
        fun `and list has one supported brand then state should be a single unreliable brand`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("visa")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.LOCAL,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.SingleUnreliableBrand(
                createCardBrandData().copy(cardBrand = CardBrand("visa")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and list has one supported brand and unsupported brands then state should be a single unreliable brand`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("amex"), isSupported = false),
                createDetectedCardType().copy(cardBrand = CardBrand("mc")),
                createDetectedCardType().copy(cardBrand = CardBrand("visa"), isSupported = false),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.LOCAL,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.SingleUnreliableBrand(
                createCardBrandData().copy(cardBrand = CardBrand("mc")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and list has multiple supported brands then state should be a single unreliable brand with first brand in the list`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("amex")),
                createDetectedCardType().copy(cardBrand = CardBrand("mc")),
                createDetectedCardType().copy(cardBrand = CardBrand("visa")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.LOCAL,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.SingleUnreliableBrand(
                createCardBrandData().copy(cardBrand = CardBrand("amex")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }
    }

    @Nested
    @DisplayName("when intent is UpdateDetectedCardTypes and detection source is network")
    inner class UpdateDetectedCardTypesNetworkTest {

        @Test
        fun `and list is empty then state should be no brands detected`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                emptyList(),
                DetectedCardTypeList.Source.LOCAL,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            assertEquals(CardBrandState.NoBrandsDetected, actual.cardBrandState)
        }

        @Test
        fun `and list has only unsupported brands then state should be unsupported brand`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("visa"), isSupported = false),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            assertEquals(CardBrandState.UnsupportedBrand, actual.cardBrandState)
        }

        @Test
        fun `and list has one supported brand then state should be a single reliable brand`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("visa")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.SingleReliableBrand(
                createCardBrandData().copy(cardBrand = CardBrand("visa")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and list has one supported brand and unsupported brands then state should be a single reliable brand`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("amex"), isSupported = false),
                createDetectedCardType().copy(cardBrand = CardBrand("mc")),
                createDetectedCardType().copy(cardBrand = CardBrand("visa"), isSupported = false),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.SingleReliableBrand(
                createCardBrandData().copy(cardBrand = CardBrand("mc")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and list has two supported brands with no shopper selection allowed then state should be a dual branded`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("amex")),
                createDetectedCardType().copy(cardBrand = CardBrand("mc")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.DualBrand(
                listOf(
                    createCardBrandData().copy(cardBrand = CardBrand("amex")),
                    createCardBrandData().copy(cardBrand = CardBrand("mc")),
                ),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and list has more than two supported brands with no shopper selection allowed then state should be a dual branded with first two brands only`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("visa")),
                createDetectedCardType().copy(cardBrand = CardBrand("mc")),
                createDetectedCardType().copy(cardBrand = CardBrand("amex")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.DualBrand(
                listOf(
                    createCardBrandData().copy(cardBrand = CardBrand("visa")),
                    createCardBrandData().copy(cardBrand = CardBrand("mc")),
                ),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and list has two supported brands with shopper selection allowed then state should be a dual branded with shopper selection allowed`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.SingleReliableBrand(createCardBrandData()),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(cardBrand = CardBrand("amex")),
                createDetectedCardType().copy(
                    cardBrand = CardBrand("mc"),
                    isShopperSelectionAllowedInDualBranded = true,
                ),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = listOf(
                    createCardBrandData().copy(cardBrand = CardBrand("amex")),
                    createCardBrandData().copy(cardBrand = CardBrand("mc")),
                ),
                shopperSelectedCardBrandData = createCardBrandData().copy(cardBrand = CardBrand("amex")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }

        @Test
        fun `and flow is dual branded with shopper selection and card brand list has not changed then selected brand is preserved`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                    cardBrandDataList = listOf(
                        createCardBrandData().copy(cardBrand = CardBrand("visa")),
                        createCardBrandData().copy(cardBrand = CardBrand("amex")),
                    ),
                    shopperSelectedCardBrandData = createCardBrandData().copy(cardBrand = CardBrand("amex")),
                ),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(
                    cardBrand = CardBrand("visa"),
                    isShopperSelectionAllowedInDualBranded = true,
                ),
                createDetectedCardType().copy(cardBrand = CardBrand("amex")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            // whole state should not change
            assertEquals(state, actual)
        }

        @Test
        fun `and flow is dual branded with shopper selection and card brand list has changed then selected brand is first one`() {
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                    cardBrandDataList = listOf(
                        createCardBrandData().copy(cardBrand = CardBrand("visa")),
                        createCardBrandData().copy(cardBrand = CardBrand("amex")),
                    ),
                    shopperSelectedCardBrandData = createCardBrandData().copy(cardBrand = CardBrand("amex")),
                ),
            )
            val detectedCardTypes = listOf(
                createDetectedCardType().copy(
                    cardBrand = CardBrand("visa"),
                    isShopperSelectionAllowedInDualBranded = true,
                ),
                createDetectedCardType().copy(cardBrand = CardBrand("mc")),
            )
            val detectedCardTypeList = DetectedCardTypeList(
                detectedCardTypes,
                DetectedCardTypeList.Source.NETWORK,
                null,
            )

            val actual = cardBrandIntentsHandler.onUpdateDetectedCardTypes(
                state,
                CardIntent.UpdateDetectedCardTypes(detectedCardTypeList),
            )

            val expectedState = CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = listOf(
                    createCardBrandData().copy(cardBrand = CardBrand("visa")),
                    createCardBrandData().copy(cardBrand = CardBrand("mc")),
                ),
                shopperSelectedCardBrandData = createCardBrandData().copy(cardBrand = CardBrand("visa")),
            )

            assertEquals(expectedState, actual.cardBrandState)
        }
    }

    // CVC / expiry date policy tests

    @Test
    fun `when card brand has expiryDatePolicy REQUIRED, then expiryDate requirementPolicy is Required`() {
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(expiryDatePolicy = Brand.FieldPolicy.REQUIRED),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Required, actual.expiryDate.requirementPolicy)
    }

    @Test
    fun `when card brand has expiryDatePolicy OPTIONAL, then expiryDate requirementPolicy is Optional`() {
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(expiryDatePolicy = Brand.FieldPolicy.OPTIONAL),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Optional, actual.expiryDate.requirementPolicy)
    }

    @Test
    fun `when card brand has expiryDatePolicy HIDDEN, then expiryDate requirementPolicy is Hidden`() {
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(expiryDatePolicy = Brand.FieldPolicy.HIDDEN),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.expiryDate.requirementPolicy)
    }

    @Test
    fun `when no card brand is detected then expiryDate requirementPolicy defaults to Required`() {
        val cardBrandState = CardBrandState.NoBrandsDetected

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Required, actual.expiryDate.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is ALWAYS_SHOW and detected card type has cvcPolicy REQUIRED, then securityCode requirementPolicy is Required`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.REQUIRED),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is ALWAYS_SHOW and detected card type has cvcPolicy OPTIONAL, then securityCode requirementPolicy is Optional`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Optional, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is ALWAYS_SHOW and detected card type has cvcPolicy HIDDEN, then securityCode requirementPolicy is Hidden`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.HIDDEN),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is HIDE_FIRST and detected card type has cvcPolicy REQUIRED, then securityCode requirementPolicy is Required`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.REQUIRED),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is HIDE_FIRST and detected card type has cvcPolicy OPTIONAL, then securityCode requirementPolicy is Optional`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Optional, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is HIDE_FIRST and detected card type has cvcPolicy HIDDEN, then securityCode requirementPolicy is Hidden`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.HIDDEN),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is ALWAYS_HIDE and detected card type has cvcPolicy REQUIRED, then securityCode requirementPolicy is Hidden`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_HIDE)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.REQUIRED),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when cvcVisibility is ALWAYS_HIDE and detected card type has cvcPolicy OPTIONAL, then securityCode requirementPolicy is Hidden`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_HIDE)
        val cardBrandState = CardBrandState.SingleReliableBrand(
            createCardBrandData().copy(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
        )

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when no card brand is detected and cvcVisibility is ALWAYS_SHOW, then securityCode requirementPolicy defaults to Required`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_SHOW)
        val cardBrandState = CardBrandState.NoBrandsDetected

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Required, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when no card brand is detected  and cvcVisibility is HIDE_FIRST, then securityCode requirementPolicy defaults to Hidden`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.HIDE_FIRST)
        val cardBrandState = CardBrandState.NoBrandsDetected

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
    }

    @Test
    fun `when no card brand is detected  and cvcVisibility is ALWAYS_HIDE, then securityCode requirementPolicy defaults to Hidden`() {
        whenever(cardComponentParams.cvcVisibility).thenReturn(CVCVisibility.ALWAYS_HIDE)
        val cardBrandState = CardBrandState.NoBrandsDetected

        val actual = cardBrandIntentsHandler.getUpdatedCardComponentState(createInitialState(), cardBrandState)

        assertEquals(RequirementPolicy.Hidden, actual.securityCode.requirementPolicy)
    }

    @Nested
    @DisplayName("when intent is SelectBrand")
    inner class SelectBrandTest {
        @Test
        fun `then selectedCardBrand is updated`() {
            val visaCardBrandData = createCardBrandData().copy(cardBrand = CardBrand("visa"))
            val carteBancaireCardBrandData = createCardBrandData().copy(cardBrand = CardBrand("cartebancaire"))
            val state = createInitialState().copy(
                cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                    cardBrandDataList = listOf(visaCardBrandData, carteBancaireCardBrandData),
                    shopperSelectedCardBrandData = visaCardBrandData,
                ),
            )

            val actual =
                cardBrandIntentsHandler.onBrandSelected(state, CardIntent.SelectBrand(CardBrand("cartebancaire")))

            assertInstanceOf<CardBrandState.DualBrandWithShopperSelection>(actual.cardBrandState)
            assertEquals(carteBancaireCardBrandData, actual.cardBrandState.shopperSelectedCardBrandData)
        }
    }

    private fun createDetectedCardType(): DetectedCardType {
        return DetectedCardType(
            cardBrand = CardBrand("visa"),
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = true,
            isShopperSelectionAllowedInDualBranded = false,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }

    private fun createInitialState() = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        socialSecurityNumber = TextInputComponentState(),
        kcpCardPassword = TextInputComponentState(),
        kcpBirthDateOrTaxNumber = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
    )

    private fun createCardBrandData() = CardBrandData(
        cardBrand = CardBrand(""),
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        panLength = null,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}
