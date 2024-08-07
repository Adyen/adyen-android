package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.data.api.OrderStatusRepository
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.internal.ui.model.DropInParams
import com.adyen.checkout.test.LoggingExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DropInViewModelTest(
    @Mock private val bundleHandler: DropInSavedStateHandleContainer,
    @Mock private val orderStatusRepository: OrderStatusRepository,
) {

    private lateinit var viewModel: DropInViewModel

    @BeforeEach
    fun beforeEach() {
        whenever(bundleHandler.checkoutConfiguration) doReturn mock()
        whenever(bundleHandler.serviceComponentName) doReturn mock()
    }

    @ParameterizedTest
    @MethodSource("getPaymentMethodsSource")
    fun `when getPaymentMethods is called, then ignored payment methods are missing`(
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        expectedPaymentMethodsList: List<PaymentMethod>,
    ) {
        whenever(bundleHandler.paymentMethodsApiResponse) doReturn paymentMethodsApiResponse
        viewModel = createDropInViewModel()

        val result = viewModel.getPaymentMethods()

        assertEquals(expectedPaymentMethodsList, result)
    }

    @ParameterizedTest
    @MethodSource("shouldSkipToSinglePaymentMethodSource")
    fun `when payment methods response contains, then should skip to component`(
        skipListWhenSinglePaymentMethodConfig: Boolean,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        expected: Boolean,
    ) {
        whenever(bundleHandler.paymentMethodsApiResponse) doReturn paymentMethodsApiResponse
        viewModel = createDropInViewModel(createDropInParams(skipListWhenSinglePaymentMethodConfig))

        val result = viewModel.shouldSkipToSinglePaymentMethod()

        assertEquals(expected, result)
    }

    private fun createDropInViewModel(
        dropInParams: DropInParams = createDropInParams(),
    ) = DropInViewModel(
        bundleHandler = bundleHandler,
        orderStatusRepository = orderStatusRepository,
        analyticsManager = TestAnalyticsManager(),
        initialDropInParams = dropInParams,
    )

    private fun createDropInParams(
        skipListWhenSinglePaymentMethod: Boolean = false,
    ) = DropInParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = "test",
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, "test"),
        amount = Amount("USD", 10),
        showPreselectedStoredPaymentMethod = true,
        skipListWhenSinglePaymentMethod = skipListWhenSinglePaymentMethod,
        isRemovingStoredPaymentMethodsEnabled = true,
        additionalDataForDropInService = null,
        overriddenPaymentMethodInformation = emptyMap(),
    )

    companion object {

        @JvmStatic
        fun getPaymentMethodsSource() = listOf(
            // paymentMethodsApiResponse, expectedPaymentMethodsList
            // Single non-ignored payment method
            arguments(
                PaymentMethodsApiResponse(paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.SCHEME))),
                listOf(PaymentMethod(PaymentMethodTypes.SCHEME)),
            ),

            // Stored payment methods
            arguments(
                PaymentMethodsApiResponse(
                    storedPaymentMethods = listOf(
                        StoredPaymentMethod(PaymentMethodTypes.SCHEME),
                        StoredPaymentMethod(PaymentMethodTypes.TWINT),
                    ),
                ),
                listOf<PaymentMethod>(),
            ),

            // Single non-ignored payment method with stored payment methods
            arguments(
                PaymentMethodsApiResponse(
                    paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.SCHEME)),
                    storedPaymentMethods = listOf(StoredPaymentMethod(PaymentMethodTypes.TWINT)),
                ),
                listOf(PaymentMethod(PaymentMethodTypes.SCHEME)),
            ),

            // Multiple non-ignored payment methods
            arguments(
                PaymentMethodsApiResponse(
                    paymentMethods = listOf(
                        PaymentMethod(PaymentMethodTypes.SCHEME),
                        PaymentMethod(PaymentMethodTypes.UPI),
                    ),
                ),
                listOf(
                    PaymentMethod(PaymentMethodTypes.SCHEME),
                    PaymentMethod(PaymentMethodTypes.UPI),
                ),
            ),

            // Single ignored payment method
            arguments(
                PaymentMethodsApiResponse(paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.UPI_QR))),
                listOf<PaymentMethod>(),
            ),
            arguments(
                PaymentMethodsApiResponse(paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.UPI_INTENT))),
                listOf<PaymentMethod>(),
            ),
            arguments(
                PaymentMethodsApiResponse(paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.UPI_COLLECT))),
                listOf<PaymentMethod>(),
            ),

            // Multiple ignored payment methods
            arguments(
                PaymentMethodsApiResponse(
                    paymentMethods = listOf(
                        PaymentMethod(PaymentMethodTypes.UPI_QR),
                        PaymentMethod(PaymentMethodTypes.UPI_INTENT),
                        PaymentMethod(PaymentMethodTypes.UPI_COLLECT),
                    ),
                ),
                listOf<PaymentMethod>(),
            ),

            // Multiple ignored payment methods with stored payment methods
            arguments(
                PaymentMethodsApiResponse(
                    paymentMethods = listOf(
                        PaymentMethod(PaymentMethodTypes.UPI_QR),
                        PaymentMethod(PaymentMethodTypes.UPI_INTENT),
                        PaymentMethod(PaymentMethodTypes.UPI_COLLECT),
                    ),
                    storedPaymentMethods = listOf(StoredPaymentMethod(PaymentMethodTypes.TWINT)),
                ),
                listOf<PaymentMethod>(),
            ),

            // Multiple payment methods, but partially ignored
            arguments(
                PaymentMethodsApiResponse(
                    paymentMethods = listOf(
                        PaymentMethod(PaymentMethodTypes.SCHEME),
                        PaymentMethod(PaymentMethodTypes.UPI_INTENT),
                        PaymentMethod(PaymentMethodTypes.UPI),
                        PaymentMethod(PaymentMethodTypes.UPI_COLLECT),
                    ),
                    storedPaymentMethods = listOf(StoredPaymentMethod(PaymentMethodTypes.TWINT)),
                ),
                listOf(
                    PaymentMethod(PaymentMethodTypes.SCHEME),
                    PaymentMethod(PaymentMethodTypes.UPI),
                ),
            ),
        )

        @JvmStatic
        fun shouldSkipToSinglePaymentMethodSource() = listOf(
            // skipListWhenSinglePaymentMethodConfig, paymentMethodsApiResponse, expected
            // Disabled in configuration
            arguments(
                false,
                PaymentMethodsApiResponse(paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.SCHEME))),
                false,
            ),

            // Stored payment method available
            arguments(
                true,
                PaymentMethodsApiResponse(
                    storedPaymentMethods = listOf(StoredPaymentMethod(type = PaymentMethodTypes.SCHEME)),
                    paymentMethods = listOf(PaymentMethod(PaymentMethodTypes.SCHEME)),
                ),
                false,
            ),

            // Multiple payment methods available
            arguments(
                true,
                PaymentMethodsApiResponse(
                    paymentMethods = listOf(
                        PaymentMethod(PaymentMethodTypes.SCHEME),
                        PaymentMethod(PaymentMethodTypes.ACH),
                    ),
                ),
                false,
            ),

            // No payment methods available
            arguments(
                true,
                PaymentMethodsApiResponse(paymentMethods = listOf()),
                false,
            ),

            // No component available for payment method
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf("UNSUPPORTED PM")),
                false,
            ),

            // Payment methods that are either action only or have no UI
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.DUIT_NOW)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.GOOGLE_PAY)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.GOOGLE_PAY_LEGACY)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.IDEAL)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.MULTIBANCO)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.PAY_NOW)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.PIX)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.PROMPT_PAY)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.TWINT)),
                false,
            ),
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.WECHAT_PAY_SDK)),
                false,
            ),

            // Supported payment method
            arguments(
                true,
                createPayPaymentMethodsApiResponse(listOf(PaymentMethodTypes.SCHEME)),
                true,
            ),
        )

        private fun createPayPaymentMethodsApiResponse(paymentMethodTypes: List<String>): PaymentMethodsApiResponse =
            PaymentMethodsApiResponse(paymentMethods = paymentMethodTypes.map { PaymentMethod(type = it) })
    }
}
