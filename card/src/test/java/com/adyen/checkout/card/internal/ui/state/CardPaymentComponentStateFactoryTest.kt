/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.data.Address
import com.adyen.checkout.core.components.data.Installments
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.paymentmethod.CardDetails
import com.adyen.checkout.cse.EncryptedCard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class CardPaymentComponentStateFactoryTest(
    @param:Mock private val sdkDataProvider: SdkDataProvider,
) {

    @BeforeEach
    fun beforeEach() {
        whenever(sdkDataProvider.createEncodedSdkData(any())).thenReturn("sdk_data")
    }

    @Test
    fun `when state is mapped then the encrypted card and the sdk data are part of the card details`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createPaymentComponentState(
            cardComponentState = createCardComponentState(),
            encryptedCard = createEncryptedCard(),
            encryptedKcpCardPassword = null,
        )

        // THEN
        assertEquals(createExpectedCardDetails(), state.data.paymentMethod)
    }

    @Test
    fun `when state is mapped then it is valid`() {
        // GIVEN
        val factory = createFactory()

        // WHEN
        val state = factory.createPaymentComponentState(
            cardComponentState = createCardComponentState(),
            encryptedCard = createEncryptedCard(),
            encryptedKcpCardPassword = null,
        )

        // THEN
        assertTrue(state.isValid)
    }

    @Nested
    @DisplayName("when creating the card details")
    inner class CardDetailsTest {

        @Test
        fun `and funding source is set then it is part of the card details`() {
            // GIVEN
            val factory = createFactory(fundingSource = "debit")

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = createCardComponentState(),
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(createExpectedCardDetails(fundingSource = "debit"), state.data.paymentMethod)
        }

        @Test
        fun `and holder name is required then it is part of the card details`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                holderName = TextInputComponentState(text = "John Doe", requirementPolicy = RequirementPolicy.Required),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(createExpectedCardDetails(holderName = "John Doe"), state.data.paymentMethod)
        }

        @Test
        fun `and holder name is hidden then it is not part of the card details`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                holderName = TextInputComponentState(text = "John Doe", requirementPolicy = RequirementPolicy.Hidden),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(createExpectedCardDetails(holderName = null), state.data.paymentMethod)
        }

        @Test
        fun `and kcp fields are required then the encrypted password and the tax number are part of the details`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                kcpBirthDateOrTaxNumber = TextInputComponentState(
                    text = "260403",
                    requirementPolicy = RequirementPolicy.Required,
                ),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = "encrypted_password",
            )

            // THEN
            val expected = createExpectedCardDetails(
                encryptedPassword = "encrypted_password",
                taxNumber = "260403",
            )
            assertEquals(expected, state.data.paymentMethod)
        }

        @Test
        fun `and kcp fields are hidden then the tax number is not part of the card details`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                kcpBirthDateOrTaxNumber = TextInputComponentState(
                    text = "260403",
                    requirementPolicy = RequirementPolicy.Hidden,
                ),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(createExpectedCardDetails(taxNumber = null), state.data.paymentMethod)
        }
    }

    @ParameterizedTest
    @MethodSource("cardBrandSource")
    fun `when state is mapped then the brand is only set when it is reliable or selected by the shopper`(
        cardBrandState: CardBrandState,
        expectedBrand: String?,
    ) {
        // GIVEN
        val factory = createFactory()
        val cardComponentState = createCardComponentState().copy(cardBrandState = cardBrandState)

        // WHEN
        val state = factory.createPaymentComponentState(
            cardComponentState = cardComponentState,
            encryptedCard = createEncryptedCard(),
            encryptedKcpCardPassword = null,
        )

        // THEN
        assertEquals(createExpectedCardDetails(brand = expectedBrand), state.data.paymentMethod)
    }

    @Nested
    @DisplayName("when creating the payment component data")
    inner class PaymentComponentDataTest {

        @Test
        fun `and store payment method is visible then it is part of the payment data`() {
            // GIVEN
            val factory = createFactory(
                componentParams = createCardComponentParams().copy(showStorePaymentMethod = true),
            )
            val cardComponentState = createCardComponentState().copy(storePaymentMethod = true)

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(true, state.data.storePaymentMethod)
        }

        @Test
        fun `and store payment method is visible but not checked then it is part of the payment data as false`() {
            // GIVEN
            val factory = createFactory(
                componentParams = createCardComponentParams().copy(showStorePaymentMethod = true),
            )
            val cardComponentState = createCardComponentState().copy(storePaymentMethod = false)

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(false, state.data.storePaymentMethod)
        }

        @Test
        fun `and store payment method is hidden then it is not part of the payment data`() {
            // GIVEN
            val factory = createFactory(
                componentParams = createCardComponentParams().copy(showStorePaymentMethod = false),
            )
            val cardComponentState = createCardComponentState().copy(storePaymentMethod = true)

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertNull(state.data.storePaymentMethod)
        }

        @Test
        fun `and postal code is required then it is part of the billing address`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                postalCode = TextInputComponentState(text = "1234 AB", requirementPolicy = RequirementPolicy.Required),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(Address(postalCode = "1234 AB"), state.data.billingAddress)
        }

        @Test
        fun `and postal code is hidden then there is no billing address`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                postalCode = TextInputComponentState(text = "1234 AB", requirementPolicy = RequirementPolicy.Hidden),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertNull(state.data.billingAddress)
        }

        @Test
        fun `and social security number is required then it is part of the payment data`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                socialSecurityNumber = TextInputComponentState(
                    text = "12312312312",
                    requirementPolicy = RequirementPolicy.Required,
                ),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals("12312312312", state.data.socialSecurityNumber)
        }

        @Test
        fun `and social security number is hidden then it is not part of the payment data`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                socialSecurityNumber = TextInputComponentState(
                    text = "12312312312",
                    requirementPolicy = RequirementPolicy.Hidden,
                ),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertNull(state.data.socialSecurityNumber)
        }

        @Test
        fun `and a regular installment is selected then it is part of the payment data`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                installmentState = createInstallmentState(
                    InstallmentModel.Regular(
                        numberOfInstallments = 3,
                        amountPerInstallment = null,
                        showAmount = false,
                    ),
                ),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(Installments(plan = "regular", value = 3), state.data.installments)
        }

        @Test
        fun `and a revolving installment is selected then the value is always one`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                installmentState = createInstallmentState(InstallmentModel.Revolving),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertEquals(Installments(plan = "revolving", value = 1), state.data.installments)
        }

        @Test
        fun `and the one time installment is selected then installments are not part of the payment data`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                installmentState = createInstallmentState(InstallmentModel.OneTime),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertNull(state.data.installments)
        }

        @Test
        fun `and no installment is selected then installments are not part of the payment data`() {
            // GIVEN
            val factory = createFactory()
            val cardComponentState = createCardComponentState().copy(
                installmentState = createInstallmentState(selectedInstallment = null),
            )

            // WHEN
            val state = factory.createPaymentComponentState(
                cardComponentState = cardComponentState,
                encryptedCard = createEncryptedCard(),
                encryptedKcpCardPassword = null,
            )

            // THEN
            assertNull(state.data.installments)
        }
    }

    private fun createFactory(
        componentParams: CardComponentParams = createCardComponentParams(),
        fundingSource: String? = null,
    ) = CardPaymentComponentStateFactory(
        componentParams = componentParams,
        sdkDataProvider = sdkDataProvider,
        paymentMethodType = PAYMENT_METHOD_TYPE,
        fundingSource = fundingSource,
    )

    private fun createEncryptedCard() = EncryptedCard(
        encryptedCardNumber = "encrypted_card_number",
        encryptedExpiryMonth = "encrypted_expiry_month",
        encryptedExpiryYear = "encrypted_expiry_year",
        encryptedSecurityCode = "encrypted_security_code",
    )

    private fun createExpectedCardDetails(
        holderName: String? = null,
        brand: String? = null,
        encryptedPassword: String? = null,
        taxNumber: String? = null,
        fundingSource: String? = null,
    ) = CardDetails(
        type = PAYMENT_METHOD_TYPE,
        sdkData = "sdk_data",
        encryptedCardNumber = "encrypted_card_number",
        encryptedExpiryMonth = "encrypted_expiry_month",
        encryptedExpiryYear = "encrypted_expiry_year",
        encryptedSecurityCode = "encrypted_security_code",
        encryptedPassword = encryptedPassword,
        holderName = holderName,
        taxNumber = taxNumber,
        brand = brand,
        fundingSource = fundingSource,
    )

    /**
     * Creates a state where every optional field is hidden, so that each test can opt in to the single field it covers.
     */
    private fun createCardComponentState() = CardComponentState(
        cardNumber = TextInputComponentState(text = "4111111111111111"),
        expiryDate = TextInputComponentState(text = "1230"),
        securityCode = TextInputComponentState(text = "737"),
        holderName = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        socialSecurityNumber = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        kcpBirthDateOrTaxNumber = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        kcpCardPassword = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        postalCode = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        showSupportedCardBrandLogos = false,
        isLoading = false,
        isCardScanningAvailable = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
        networkBinLookupState = null,
        installmentState = createInstallmentState(selectedInstallment = null),
    )

    private fun createInstallmentState(selectedInstallment: InstallmentModel?) = InstallmentState(
        installmentOptions = listOfNotNull(selectedInstallment),
        selectedInstallment = selectedInstallment,
    )

    private fun createCardComponentParams() = CardComponentParams(
        showCardholderName = false,
        supportedCardBrands = emptyList(),
        showStorePaymentMethod = false,
        showSupportedCardBrandLogos = false,
        socialSecurityNumberVisibility = FieldVisibility.HIDE,
        koreanAuthenticationVisibility = FieldVisibility.HIDE,
        showPostalCode = false,
        cvcVisibility = CVCVisibility.ALWAYS_HIDE,
        storedCVCVisibility = StoredCVCVisibility.HIDE,
        showCardScanner = false,
        installmentParams = null,
    )

    companion object {
        private const val PAYMENT_METHOD_TYPE = "scheme"

        private fun createCardBrandData(txVariant: String) = CardBrandData(
            cardBrand = CardBrand(txVariant),
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )

        @JvmStatic
        fun cardBrandSource() = listOf(
            // cardBrandState, expectedBrand
            arguments(CardBrandState.NoBrandsDetected, null),
            arguments(CardBrandState.UnsupportedBrand, null),
            arguments(CardBrandState.HiddenBrand, null),
            arguments(CardBrandState.SingleUnreliableBrand(createCardBrandData("visa")), null),
            arguments(CardBrandState.SingleReliableWithHiddenBrand(createCardBrandData("visa")), null),
            arguments(CardBrandState.DualBrand(listOf(createCardBrandData("visa"))), null),
            arguments(CardBrandState.SingleReliableBrand(createCardBrandData("visa")), "visa"),
            arguments(
                CardBrandState.DualBrandWithShopperSelection(
                    cardBrandDataList = listOf(createCardBrandData("visa"), createCardBrandData("cartebancaire")),
                    shopperSelectedCardBrandData = createCardBrandData("cartebancaire"),
                ),
                "cartebancaire",
            ),
        )
    }
}
