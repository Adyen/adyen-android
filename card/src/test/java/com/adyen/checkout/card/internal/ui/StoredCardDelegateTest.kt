/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/8/2022.
 */

package com.adyen.checkout.card.internal.ui

import app.cash.turbine.test
import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.R
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.card
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.InstallmentsParamsMapper
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.data.api.TestPublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.TestCardEncryptor
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class StoredCardDelegateTest(
    @Mock private val submitHandler: SubmitHandler<CardComponentState>,
    @Mock private val cardConfigDataGenerator: CardConfigDataGenerator,
) {

    private lateinit var cardEncryptor: TestCardEncryptor
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var delegate: StoredCardDelegate

    @BeforeEach
    fun before() {
        cardEncryptor = TestCardEncryptor()
        publicKeyRepository = TestPublicKeyRepository()
        analyticsManager = TestAnalyticsManager()
        delegate = createCardDelegate()

        whenever(cardConfigDataGenerator.generate(any(), any())) doReturn emptyMap()
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

    @Test
    fun `when component is initialized with cvc shown, then view flow emits StoredCardView`() = runTest {
        delegate = createCardDelegate(
            configuration = createCheckoutConfiguration {
                setHideCvcStoredCard(false)
            },
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.viewFlow.test {
            assertEquals(CardComponentViewType.StoredCardView, expectMostRecentItem())
        }
    }

    @Test
    fun `when component is initialized with cvc hidden, then view flow emits null`() = runTest {
        delegate = createCardDelegate(
            configuration = createCheckoutConfiguration {
                setHideCvcStoredCard(true)
            },
        )
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.viewFlow.test {
            assertEquals(null, expectMostRecentItem())
        }
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {
        @Test
        fun `input is empty with default config, then output data should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
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
                configuration = getCustomCardConfiguration(),
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
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
                delegate.updateInputData {
                    securityCode = TEST_SECURITY_CODE
                }

                with(expectMostRecentItem()) {
                    assertTrue(isValid)
                    assertEquals(FieldState(TEST_SECURITY_CODE, Validation.Valid), securityCodeState)
                    assertEquals(createOutputData(), this)
                }
            }
        }

        @Test
        fun `security code is empty with hide cvc stored config, then output data should be valid`() = runTest {
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setHideCvcStoredCard(true)
                },
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
                    assertTrue(isValid)
                }
            }
        }

        @Test
        fun `security code is empty with a no cvc card, then output data should be valid`() = runTest {
            delegate = createCardDelegate(
                storedPaymentMethod = getStoredPaymentMethod(
                    brand = CardType.BCMC.txVariant,
                ),
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
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
                delegate.updateInputData { /* Empty to trigger an update */ }

                val componentState = expectMostRecentItem()

                assertFalse(componentState.isReady)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid and analytics error event is tracked `() =
            runTest {
                cardEncryptor.shouldThrowException = true

                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.componentStateFlow.test {
                    delegate.updateComponentState(createOutputData())

                    val componentState = expectMostRecentItem()

                    val expectedEvent =
                        GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
                    analyticsManager.assertLastEventEquals(expectedEvent)

                    assertTrue(componentState.isReady)
                    assertFalse(componentState.isInputValid)
                    assertNull(componentState.lastFourDigits)
                }
            }

        @Test
        fun `security code in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(
                        securityCodeState = FieldState(
                            "12",
                            Validation.Invalid(R.string.checkout_security_code_not_valid),
                        ),
                    ),
                )

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `output data with default config is valid, then component state should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(createOutputData())

                val componentState = expectMostRecentItem()

                with(componentState) {
                    assertTrue(isValid)
                    assertEquals(TEST_CARD_LAST_FOUR, lastFourDigits)
                    assertEquals("", binValue)
                    assertEquals(CardBrand(cardType = CardType.MASTERCARD), cardBrand)
                }

                val paymentComponentData = componentState.data
                with(paymentComponentData) {
                    assertNull(storePaymentMethod)
                    assertEquals(TEST_ORDER, order)
                    assertNull(shopperReference)
                    assertNull(socialSecurityNumber)
                    assertNull(billingAddress)
                    assertNull(installments)
                    assertNull(amount)
                    assertNull(dateOfBirth)
                    assertNull(deliveryAddress)
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
                    assertEquals(ThreeDS2Service.INSTANCE.sdkVersion, threeDS2SdkVersion)
                }
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.card.internal.ui.StoredCardDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(configurationValue)
                delegate = createCardDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    securityCode = TEST_SECURITY_CODE
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(true)
                },
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    @Nested
    inner class SubmitHandlerTest {

        @Test
        fun `when delegate is initialized then submit handler event is initialized`() = runTest {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            delegate.initialize(coroutineScope)
            verify(submitHandler).initialize(coroutineScope, delegate.componentStateFlow)
        }

        @Test
        fun `when delegate setInteractionBlocked is called then submit handler setInteractionBlocked is called`() =
            runTest {
                delegate.setInteractionBlocked(true)
                verify(submitHandler).setInteractionBlocked(true)
            }

        @Test
        fun `when delegate onSubmit is called then submit handler onSubmit is called`() = runTest {
            delegate.componentStateFlow.test {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.onSubmit()
                verify(submitHandler).onSubmit(expectMostRecentItem())
            }
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when delegate is initialized then analytics manager is initialized`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            analyticsManager.assertIsInitialized()
        }

        @Test
        fun `when delegate is initialized, then render event is tracked`() {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val expectedEvent = GenericEvents.rendered(
                component = PaymentMethodTypes.SCHEME,
                isStoredPaymentMethod = true,
                configData = emptyMap(),
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when submitFlow emits an event, then submit event is tracked`() = runTest {
            val submitFlow = flow<CardComponentState> { emit(mock()) }
            whenever(submitHandler.submitFlow) doReturn submitFlow
            val delegate = createCardDelegate()

            delegate.submitFlow.collectLatest {
                val expectedEvent = GenericEvents.submit(PaymentMethodTypes.SCHEME)
                analyticsManager.assertLastEventEquals(expectedEvent)
            }
        }

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            analyticsManager.setCheckoutAttemptId(TEST_CHECKOUT_ATTEMPT_ID)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    securityCode = TEST_SECURITY_CODE
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }

        @Test
        fun `when fetching the public key fails, then an error event is tracked`() = runTest {
            publicKeyRepository.shouldReturnError = true
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            val expectedEvent = GenericEvents.error(CardPaymentMethod.PAYMENT_METHOD_TYPE, ErrorEvent.API_PUBLIC_KEY)
            analyticsManager.assertLastEventEquals(expectedEvent)
        }

        @Test
        fun `when delegate is cleared then analytics manager is cleared`() {
            delegate.onCleared()

            analyticsManager.assertIsCleared()
        }
    }

    @Suppress("LongParameterList")
    private fun createCardDelegate(
        publicKeyRepository: PublicKeyRepository = this.publicKeyRepository,
        cardEncryptor: BaseCardEncryptor = this.cardEncryptor,
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        storedPaymentMethod: StoredPaymentMethod = getStoredPaymentMethod(),
        analyticsManager: AnalyticsManager = this.analyticsManager,
        submitHandler: SubmitHandler<CardComponentState> = this.submitHandler,
        order: OrderRequest? = TEST_ORDER,
    ): StoredCardDelegate {
        val componentParams = CardComponentParamsMapper(
            commonComponentParamsMapper = CommonComponentParamsMapper(),
            installmentsParamsMapper = InstallmentsParamsMapper(),
        ).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            StoredPaymentMethod(),
        )

        return StoredCardDelegate(
            observerRepository = PaymentObserverRepository(),
            storedPaymentMethod = storedPaymentMethod,
            publicKeyRepository = publicKeyRepository,
            componentParams = componentParams,
            cardEncryptor = cardEncryptor,
            analyticsManager = analyticsManager,
            submitHandler = submitHandler,
            order = order,
            cardConfigDataGenerator = cardConfigDataGenerator,
            cardValidationMapper = CardValidationMapper(),
        )
    }

    @Suppress("LongParameterList")
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

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: CardConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        card(configuration)
    }

    private fun getCustomCardConfiguration() = createCheckoutConfiguration {
        setHideCvc(true)
        setShopperReference("shopper_android")
        setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
        setInstallmentConfigurations(
            InstallmentConfiguration(
                InstallmentOptions.DefaultInstallmentOptions(
                    maxInstallments = 3,
                    includeRevolving = true,
                ),
            ),
        )
        setHolderNameRequired(true)
        setAddressConfiguration(AddressConfiguration.FullAddress())
        setKcpAuthVisibility(KCPAuthVisibility.SHOW)
        setSupportedCardTypes(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS)
    }

    @Suppress("LongParameterList")
    private fun createOutputData(
        cardNumberState: FieldState<String> = FieldState(TEST_CARD_LAST_FOUR, Validation.Valid),
        expiryDateState: FieldState<ExpiryDate> = FieldState(TEST_EXPIRY_DATE, Validation.Valid),
        securityCodeState: FieldState<String> = FieldState(TEST_SECURITY_CODE, Validation.Valid),
        holderNameState: FieldState<String> = FieldState("", Validation.Valid),
        socialSecurityNumberState: FieldState<String> = FieldState("", Validation.Valid),
        kcpBirthDateOrTaxNumberState: FieldState<String> = FieldState("", Validation.Valid),
        kcpCardPasswordState: FieldState<String> = FieldState("", Validation.Valid),
        addressState: AddressOutputData = AddressValidationUtils.makeValidEmptyAddressOutput(
            AddressInputModel(),
        ),
        installmentState: FieldState<InstallmentModel?> = FieldState(null, Validation.Valid),
        shouldStorePaymentMethod: Boolean = false,
        cvcUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
        expiryDateUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
        holderNameUIState: InputFieldUIState = InputFieldUIState.HIDDEN,
        showStorePaymentField: Boolean = false,
        detectedCardTypes: List<DetectedCardType> = listOf(createDetectedCardType()),
        isSocialSecurityNumberRequired: Boolean = false,
        isKCPAuthRequired: Boolean = false,
        addressUIState: AddressFormUIState = AddressFormUIState.NONE,
        installmentOptions: List<InstallmentModel> = emptyList(),
        cardBrands: List<CardListItem> = emptyList(),
        isCardListVisible: Boolean = false
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
            shouldStorePaymentMethod = shouldStorePaymentMethod,
            cvcUIState = cvcUIState,
            expiryDateUIState = expiryDateUIState,
            holderNameUIState = holderNameUIState,
            showStorePaymentField = showStorePaymentField,
            detectedCardTypes = detectedCardTypes,
            isSocialSecurityNumberRequired = isSocialSecurityNumberRequired,
            isKCPAuthRequired = isKCPAuthRequired,
            addressUIState = addressUIState,
            installmentOptions = installmentOptions,
            cardBrands = cardBrands,
            isDualBranded = false,
            kcpBirthDateOrTaxNumberHint = null,
            isCardListVisible = isCardListVisible,
            dualBrandCardBrands = emptyList(), // TODO
        )
    }

    @Suppress("LongParameterList")
    private fun createDetectedCardType(
        cardBrand: CardBrand = TEST_CARD_TYPE,
        isReliable: Boolean = true,
        enableLuhnCheck: Boolean = true,
        cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        isSupported: Boolean = true,
        panLength: Int? = null,
        paymentMethodVariant: String? = null,
    ): DetectedCardType {
        return DetectedCardType(
            cardBrand = cardBrand,
            isReliable = isReliable,
            enableLuhnCheck = enableLuhnCheck,
            cvcPolicy = cvcPolicy,
            expiryDatePolicy = expiryDatePolicy,
            isSupported = isSupported,
            panLength = panLength,
            paymentMethodVariant = paymentMethodVariant,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CARD_LAST_FOUR = "1234"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)
        private const val TEST_SECURITY_CODE = "737"
        private const val TEST_STORED_PM_ID = "1337"
        private val TEST_CARD_TYPE = CardBrand(cardType = CardType.MASTERCARD)
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(null, null),
            arguments(null, null),
        )
    }
}
