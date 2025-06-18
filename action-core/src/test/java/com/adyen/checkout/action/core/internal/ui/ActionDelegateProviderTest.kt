package com.adyen.checkout.action.core.internal.ui

import android.app.Application
import android.os.Parcel
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.await.internal.ui.AwaitDelegate
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.AwaitAction
import com.adyen.checkout.components.core.action.QrCodeAction
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.Threeds2Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.action.Threeds2FingerprintAction
import com.adyen.checkout.components.core.action.TwintSdkData
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.action.WeChatPaySdkData
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.util.LocaleProvider
import com.adyen.checkout.qrcode.internal.ui.QRCodeDelegate
import com.adyen.checkout.redirect.internal.ui.RedirectDelegate
import com.adyen.checkout.twint.action.internal.ui.TwintActionDelegate
import com.adyen.checkout.voucher.internal.ui.VoucherDelegate
import com.adyen.checkout.wechatpay.internal.ui.WeChatDelegate
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@ExtendWith(MockitoExtension::class)
internal class ActionDelegateProviderTest(
    @Mock private val localeProvider: LocaleProvider
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var actionDelegateProvider: ActionDelegateProvider

    @BeforeEach
    fun setup() {
        whenever(localeProvider.getLocale(any())) doReturn Locale.US
        analyticsManager = TestAnalyticsManager()
        actionDelegateProvider = ActionDelegateProvider(
            analyticsManager = analyticsManager,
            dropInOverrideParams = null,
            localeProvider = localeProvider,
        )
    }

    @ParameterizedTest
    @MethodSource("actionSource")
    fun `when action is of certain type, then related delegate is provided`(
        action: Action,
        expectedDelegate: Class<ActionDelegate>,
    ) {
        val configuration = CheckoutConfiguration(Environment.TEST, "")

        val delegate = actionDelegateProvider.getDelegate(action, configuration, SavedStateHandle(), Application())

        assertInstanceOf(expectedDelegate, delegate)
    }

    @Test
    fun `when unknown action is used, then an error will be thrown`() {
        val configuration = CheckoutConfiguration(Environment.TEST, "")

        assertThrows<CheckoutException> {
            actionDelegateProvider.getDelegate(UnknownAction(), configuration, SavedStateHandle(), Application())
        }
    }

    @Test
    fun `when sdk action  with unknown paymentMethodType is used, then an error will be thrown`() {
        val configuration = CheckoutConfiguration(Environment.TEST, "")

        assertThrows<CheckoutException> {
            actionDelegateProvider.getDelegate(
                action = SdkAction<TwintSdkData>(paymentMethodType = "test"),
                checkoutConfiguration = configuration,
                savedStateHandle = SavedStateHandle(),
                application = Application(),
            )
        }
    }

    companion object {

        @JvmStatic
        fun actionSource() = listOf(
            arguments(AwaitAction(), AwaitDelegate::class.java),
            arguments(QrCodeAction(), QRCodeDelegate::class.java),
            arguments(RedirectAction(), RedirectDelegate::class.java),
            arguments(Threeds2Action(), Adyen3DS2Delegate::class.java),
            arguments(Threeds2ChallengeAction(), Adyen3DS2Delegate::class.java),
            arguments(Threeds2FingerprintAction(), Adyen3DS2Delegate::class.java),
            arguments(VoucherAction(), VoucherDelegate::class.java),
            arguments(
                SdkAction<WeChatPaySdkData>(paymentMethodType = PaymentMethodTypes.WECHAT_PAY_SDK),
                WeChatDelegate::class.java,
            ),
            arguments(
                SdkAction<TwintSdkData>(paymentMethodType = PaymentMethodTypes.TWINT),
                TwintActionDelegate::class.java,
            ),
        )
    }

    private class UnknownAction(
        override var type: String? = null,
        override var paymentMethodType: String? = null,
        override var paymentData: String? = null,
    ) : Action() {
        override fun writeToParcel(dest: Parcel, flags: Int) = Unit
    }
}
