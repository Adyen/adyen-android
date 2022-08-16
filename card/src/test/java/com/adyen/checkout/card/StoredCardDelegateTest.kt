/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/8/2022.
 */

package com.adyen.checkout.card

import app.cash.turbine.test
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.util.AddressValidationUtils
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.test.TestCardEncrypter
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class StoredCardDelegateTest {

    private lateinit var cardEncrypter: TestCardEncrypter
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var delegate: StoredCardDelegate

    @BeforeEach
    fun before() {
        cardEncrypter = TestCardEncrypter()
        publicKeyRepository = TestPublicKeyRepository()
        delegate = createCardDelegate()
    }

    @Test
    fun `when component is not initialized, then an error is propagated`() = runTest {
        publicKeyRepository.shouldReturnError = true

        delegate.exceptionFlow.test {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val exception = expectMostRecentItem()

            assertEquals(publicKeyRepository.errorResult.exceptionOrNull(), exception.cause)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {
        @Test
        fun `input is empty with default config, then output data should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                    assertTrue(securityCodeState.validation is Validation.Invalid)
                    assertTrue(cardNumberState.validation is Validation.Valid)
                    assertTrue(expiryDateState.validation is Validation.Valid)
                    assertTrue(holderNameState.validation is Validation.Valid)
                    assertTrue(socialSecurityNumberState.validation is Validation.Valid)
                    assertTrue(kcpBirthDateOrTaxNumberState.validation is Validation.Valid)
                    assertTrue(kcpCardPasswordState.validation is Validation.Valid)
                    assertTrue(installmentState.validation is Validation.Valid)
                    assertTrue(addressState.isValid)
                }
            }
        }

        @Test
        fun `input is empty with custom config, then output data should be invalid`() = runTest {
            delegate = createCardDelegate(
                configuration = getCustomCardConfigurationBuilder().build()
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
                    assertFalse(isValid)
                    assertTrue(securityCodeState.validation is Validation.Invalid)
                    assertTrue(cardNumberState.validation is Validation.Valid)
                    assertTrue(expiryDateState.validation is Validation.Valid)
                    assertTrue(holderNameState.validation is Validation.Valid)
                    assertTrue(socialSecurityNumberState.validation is Validation.Valid)
                    assertTrue(kcpBirthDateOrTaxNumberState.validation is Validation.Valid)
                    assertTrue(kcpCardPasswordState.validation is Validation.Valid)
                    assertTrue(installmentState.validation is Validation.Valid)
                    assertTrue(addressState.isValid)
                }
            }
        }

        @Test
        fun `input data with default config is valid, then output data should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                val inputData = delegate.inputData.apply {
                    securityCode = TEST_SECURITY_CODE
                }

                delegate.onInputDataChanged(inputData)

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                    assertEquals(FieldState(TEST_SECURITY_CODE, Validation.Valid), securityCodeState)
                    assertEquals(createOutputData(), this)
                }
            }
        }

        @Test
        fun `security code is empty with hide cvc stored config, then output data should be valid`() = runTest {
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setHideCvcStoredCard(true)
                    .build()
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                }
            }
        }

        @Test
        fun `security code is empty with a no cvc card, then output data should be valid`() = runTest {
            delegate = createCardDelegate(
                storedPaymentMethod = getStoredPaymentMethod(
                    brand = CardType.BCMC.txVariant,
                )
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                }
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `component is not initialized, then component state should not be ready`() = runTest {
            delegate.componentStateFlow.test {
                delegate.createComponentState(createOutputData())

                val componentState = requireNotNull(expectMostRecentItem())

                assertFalse(componentState.isReady)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            cardEncrypter.shouldThrowException = true

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(createOutputData())

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `security code in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        securityCodeState = FieldState(
                            "12",
                            Validation.Invalid(R.string.checkout_security_code_not_valid)
                        )
                    )
                )

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `output data with default config is valid, then component state should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(createOutputData())

                val componentState = requireNotNull(expectMostRecentItem())

                with(componentState) {
                    assertTrue(isValid)
                    assertEquals(TEST_CARD_LAST_FOUR, lastFourDigits)
                    assertEquals("", binValue)
                    assertEquals(CardType.MASTERCARD, cardType)
                }

                val paymentComponentData = componentState.data
                with(paymentComponentData) {
                    assertFalse(storePaymentMethod)
                    assertNull(shopperReference)
                    assertNull(socialSecurityNumber)
                    assertNull(billingAddress)
                    assertNull(installments)
                    assertNull(amount)
                    assertNull(dateOfBirth)
                    assertNull(deliveryAddress)
                    assertNull(order)
                    assertNull(shopperEmail)
                    assertNull(shopperName)
                    assertNull(telephoneNumber)
                }

                with(requireNotNull(paymentComponentData.paymentMethod)) {
                    assertEquals(TEST_SECURITY_CODE, encryptedSecurityCode)
                    assertEquals(TEST_STORED_PM_ID, storedPaymentMethodId)
                    assertEquals(PaymentMethodTypes.SCHEME, type)
                    assertNull(encryptedCardNumber)
                    assertNull(encryptedExpiryMonth)
                    assertNull(encryptedExpiryYear)
                    assertNull(holderName)
                    assertNull(taxNumber)
                    assertNull(encryptedPassword)
                    assertNull(fundingSource)
                    assertNull(brand)
                    assertNull(threeDS2SdkVersion)
                }
            }
        }
    }

    private fun createCardDelegate(
        publicKeyRepository: PublicKeyRepository = this.publicKeyRepository,
        cardEncrypter: CardEncrypter = this.cardEncrypter,
        configuration: CardConfiguration = getDefaultCardConfigurationBuilder().build(),
        storedPaymentMethod: StoredPaymentMethod = getStoredPaymentMethod(),
    ): StoredCardDelegate {
        return StoredCardDelegate(
            storedPaymentMethod = storedPaymentMethod,
            publicKeyRepository = publicKeyRepository,
            configuration = configuration,
            cardEncrypter = cardEncrypter,
        )
    }

    private fun getStoredPaymentMethod(
        id: String = TEST_STORED_PM_ID,
        type: String = CardPaymentMethod.PAYMENT_METHOD_TYPE,
        brand: String = TEST_CARD_TYPE.txVariant,
        lastFour: String = TEST_CARD_LAST_FOUR,
        expiryMonth: String = TEST_EXPIRY_DATE.expiryMonth.toString(),
        expiryYear: String = TEST_EXPIRY_DATE.expiryYear.toString(),
    ): StoredPaymentMethod {
        return StoredPaymentMethod(
            id = id,
            type = type,
            brand = brand,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            lastFour = lastFour,
        )
    }

    private fun getDefaultCardConfigurationBuilder(): CardConfiguration.Builder {
        return CardConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
    }

    private fun getCustomCardConfigurationBuilder(): CardConfiguration.Builder {
        return CardConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
            .setHideCvc(true)
            .setShopperReference("shopper_android")
            .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
            .setInstallmentConfigurations(
                InstallmentConfiguration(
                    InstallmentOptions.DefaultInstallmentOptions(
                        maxInstallments = 3,
                        includeRevolving = true
                    )
                )
            )
            .setHolderNameRequired(true)
            .setAddressConfiguration(AddressConfiguration.FullAddress())
            .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
            .setSupportedCardTypes(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS)
    }

    private fun createOutputData(
        cardNumberState: FieldState<String> = FieldState(TEST_CARD_LAST_FOUR, Validation.Valid),
        expiryDateState: FieldState<ExpiryDate> = FieldState(TEST_EXPIRY_DATE, Validation.Valid),
        securityCodeState: FieldState<String> = FieldState(TEST_SECURITY_CODE, Validation.Valid),
        holderNameState: FieldState<String> = FieldState("", Validation.Valid),
        socialSecurityNumberState: FieldState<String> = FieldState("", Validation.Valid),
        kcpBirthDateOrTaxNumberState: FieldState<String> = FieldState("", Validation.Valid),
        kcpCardPasswordState: FieldState<String> = FieldState("", Validation.Valid),
        addressState: AddressOutputData = AddressValidationUtils.makeValidEmptyAddressOutput(AddressInputModel()),
        installmentState: FieldState<InstallmentModel?> = FieldState(null, Validation.Valid),
        isStoredPaymentMethodEnable: Boolean = false,
        cvcUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
        expiryDateUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
        holderNameUIState: InputFieldUIState = InputFieldUIState.HIDDEN,
        showStorePaymentField: Boolean = false,
        detectedCardTypes: List<DetectedCardType> = listOf(createDetectedCardType()),
        isSocialSecurityNumberRequired: Boolean = false,
        isKCPAuthRequired: Boolean = false,
        addressUIState: AddressFormUIState = AddressFormUIState.NONE,
        installmentOptions: List<InstallmentModel> = emptyList(),
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
        supportedCardTypes: List<CardType> = emptyList(),
    ): CardOutputData {
        return CardOutputData(
            cardNumberState = cardNumberState,
            expiryDateState = expiryDateState,
            securityCodeState = securityCodeState,
            holderNameState = holderNameState,
            socialSecurityNumberState = socialSecurityNumberState,
            kcpBirthDateOrTaxNumberState = kcpBirthDateOrTaxNumberState,
            kcpCardPasswordState = kcpCardPasswordState,
            addressState = addressState,
            installmentState = installmentState,
            isStoredPaymentMethodEnable = isStoredPaymentMethodEnable,
            cvcUIState = cvcUIState,
            expiryDateUIState = expiryDateUIState,
            holderNameUIState = holderNameUIState,
            showStorePaymentField = showStorePaymentField,
            detectedCardTypes = detectedCardTypes,
            isSocialSecurityNumberRequired = isSocialSecurityNumberRequired,
            isKCPAuthRequired = isKCPAuthRequired,
            addressUIState = addressUIState,
            installmentOptions = installmentOptions,
            countryOptions = countryOptions,
            stateOptions = stateOptions,
            supportedCardTypes = supportedCardTypes,
            isDualBranded = false,
            kcpBirthDateOrTaxNumberHint = null,
            componentMode = ComponentMode.STORED,
        )
    }

    private fun createDetectedCardType(
        cardType: CardType = TEST_CARD_TYPE,
        isReliable: Boolean = true,
        enableLuhnCheck: Boolean = true,
        cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        isSupported: Boolean = true,
        isSelected: Boolean = false,
    ): DetectedCardType {
        return DetectedCardType(
            cardType = cardType,
            isReliable = isReliable,
            enableLuhnCheck = enableLuhnCheck,
            cvcPolicy = cvcPolicy,
            expiryDatePolicy = expiryDatePolicy,
            isSupported = isSupported,
            isSelected = isSelected,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CARD_LAST_FOUR = "1234"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)
        private const val TEST_SECURITY_CODE = "737"
        private const val TEST_STORED_PM_ID = "1337"
        private val TEST_CARD_TYPE = CardType.MASTERCARD
    }
}
