/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2026.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.helper.CardConfigDataGenerator
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.StoredCardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.StoredCardViewStateProducer
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class StoredCardComponentTest(
    @param:Mock private val cardEncryptor: BaseCardEncryptor,
    @param:Mock private val sdkDataProvider: SdkDataProvider,
    @param:Mock private val cardConfigDataGenerator: CardConfigDataGenerator,
) {

    private lateinit var analyticsManager: TestAnalyticsManager

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
    }

    @Test
    fun `when component is initialized then rendered event is tracked`() {
        // GIVEN
        val configData = mapOf("testKey" to "testValue")
        whenever(cardConfigDataGenerator.generate(params = any(), isStored = eq(true))).thenReturn(configData)
        analyticsManager = TestAnalyticsManager()

        // WHEN
        createComponent()

        // THEN
        val expected = GenericEvents.rendered(
            component = PAYMENT_METHOD_TYPE,
            isStoredPaymentMethod = true,
            configData = configData,
        )
        analyticsManager.assertHasEventEquals(expected)
    }

    @Nested
    @DisplayName("when submit is called")
    inner class SubmitTest {

        @Test
        fun `and state is valid and publicKey is present then Submit event is emitted`() = runTest {
            // GIVEN
            val encryptedCard = EncryptedCard(
                encryptedCardNumber = null,
                encryptedExpiryMonth = null,
                encryptedExpiryYear = null,
                encryptedSecurityCode = "encrypted_cvc",
            )
            whenever(cardEncryptor.encryptFields(any(), any())).thenReturn(encryptedCard)
            whenever(sdkDataProvider.createEncodedSdkData(any())).thenReturn("sdk_data")
            val component = createComponent(
                storedCVCVisibility = StoredCVCVisibility.HIDE,
                publicKey = "test_public_key",
            )
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertEquals(1, eventFlow.values.size)
            val event = eventFlow.latestValue
            assertTrue(event is PaymentComponentEvent.Submit)
            assertTrue((event as PaymentComponentEvent.Submit).state.isValid)
        }

        @Test
        fun `and state is valid but publicKey is null then Error event is emitted`() = runTest {
            // GIVEN
            val component = createComponent(
                storedCVCVisibility = StoredCVCVisibility.HIDE,
                publicKey = null,
            )
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertEquals(1, eventFlow.values.size)
            assertTrue(eventFlow.latestValue is PaymentComponentEvent.Error)
        }

        @Test
        fun `and state is valid but publicKey is null then API_PUBLIC_KEY analytics error is tracked`() = runTest {
            // GIVEN
            val component = createComponent(
                storedCVCVisibility = StoredCVCVisibility.HIDE,
                publicKey = null,
            )

            // WHEN
            component.submit()

            // THEN
            val expected = GenericEvents.error(PAYMENT_METHOD_TYPE, ErrorEvent.API_PUBLIC_KEY)
            analyticsManager.assertHasEventEquals(expected)
        }

        @Test
        fun `and state is valid but encryption fails then Error event is emitted`() = runTest {
            // GIVEN
            whenever(cardEncryptor.encryptFields(any(), any())).thenThrow(EncryptionException("test", null))
            val component = createComponent(
                storedCVCVisibility = StoredCVCVisibility.HIDE,
                publicKey = "test_public_key",
            )
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertEquals(1, eventFlow.values.size)
            assertTrue(eventFlow.latestValue is PaymentComponentEvent.Error)
        }

        @Test
        fun `and state is valid but encryption fails then ENCRYPTION analytics error is tracked`() = runTest {
            // GIVEN
            whenever(cardEncryptor.encryptFields(any(), any())).thenThrow(EncryptionException("test", null))
            val component = createComponent(
                storedCVCVisibility = StoredCVCVisibility.HIDE,
                publicKey = "test_public_key",
            )

            // WHEN
            component.submit()

            // THEN
            val expected = GenericEvents.error(PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
            analyticsManager.assertHasEventEquals(expected)
        }

        @Test
        fun `and state is invalid then no Submit event is emitted`() = runTest {
            // GIVEN - CVC is required but empty (invalid)
            val component = createComponent(
                storedCVCVisibility = StoredCVCVisibility.SHOW,
                publicKey = "test_public_key",
            )
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertTrue(eventFlow.values.isEmpty())
        }
    }

    @Nested
    @DisplayName("when requiresUserInteraction is called")
    inner class RequiresUserInteractionTest {

        @Test
        fun `and CVC is required then returns true`() {
            // GIVEN
            val component = createComponent(storedCVCVisibility = StoredCVCVisibility.SHOW)

            // WHEN
            val result = component.requiresUserInteraction()

            // THEN
            assertTrue(result)
        }

        @Test
        fun `and CVC is hidden then returns false`() {
            // GIVEN
            val component = createComponent(storedCVCVisibility = StoredCVCVisibility.HIDE)

            // WHEN
            val result = component.requiresUserInteraction()

            // THEN
            assertFalse(result)
        }
    }

    private fun createComponent(
        storedCVCVisibility: StoredCVCVisibility = StoredCVCVisibility.HIDE,
        publicKey: String? = null,
    ): StoredCardComponent {
        val storedPaymentMethod = createStoredPaymentMethod()
        val componentParams = createCardComponentParams(storedCVCVisibility = storedCVCVisibility)
        return StoredCardComponent(
            storedPaymentMethod = storedPaymentMethod,
            analyticsManager = analyticsManager,
            cardEncryptor = cardEncryptor,
            componentStateValidator = StoredCardComponentStateValidator(CardValidationMapper()),
            componentStateFactory = StoredCardComponentStateFactory(storedPaymentMethod, componentParams),
            componentStateReducer = StoredCardComponentStateReducer(),
            viewStateProducer = StoredCardViewStateProducer(),
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
            sdkDataProvider = sdkDataProvider,
            publicKey = publicKey,
            paymentMethodType = PAYMENT_METHOD_TYPE,
            componentParams = componentParams,
            cardConfigDataGenerator = cardConfigDataGenerator,
        )
    }

    private fun createStoredPaymentMethod() = StoredCardPaymentMethod(
        type = PAYMENT_METHOD_TYPE,
        name = "Test Card",
        id = "stored_pm_id",
        supportedShopperInteractions = listOf("Ecommerce"),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = "03",
        expiryYear = "2030",
        holderName = null,
        fundingSource = null,
    )

    private fun createCardComponentParams(
        storedCVCVisibility: StoredCVCVisibility = StoredCVCVisibility.HIDE,
    ) = CardComponentParams(
        showCardholderName = false,
        supportedCardBrands = emptyList(),
        showStorePaymentMethod = false,
        showSupportedCardBrandLogos = false,
        socialSecurityNumberVisibility = FieldVisibility.HIDE,
        koreanAuthenticationVisibility = FieldVisibility.HIDE,
        showPostalCode = false,
        cvcVisibility = CVCVisibility.ALWAYS_HIDE,
        storedCVCVisibility = storedCVCVisibility,
        showCardScanner = false,
        installmentParams = null,
    )

    companion object {
        private const val PAYMENT_METHOD_TYPE = "scheme"
    }
}
