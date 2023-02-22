package com.adyen.checkout.action.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import java.util.Locale

@Suppress("UNCHECKED_CAST")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class ActionHandlingPaymentMethodConfigurationBuilder<
    ConfigurationT : Configuration,
    BuilderT : BaseConfigurationBuilder<ConfigurationT, BuilderT>
    >(
    shopperLocale: Locale,
    environment: Environment,
    clientKey: String
) : BaseConfigurationBuilder<ConfigurationT, BuilderT>(shopperLocale, environment, clientKey),
    ActionHandlingConfigurationBuilder<BuilderT> {

    protected val genericActionConfigurationBuilder = GenericActionConfiguration.Builder(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
    )

    /**
     * Constructor that provides default values.
     *
     * @param context A Context
     * @param environment   The [Environment] to be used for network calls to Adyen.
     * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
     */
    constructor(
        context: Context,
        environment: Environment,
        clientKey: String
    ) : this(
        LocaleUtil.getLocale(context),
        environment,
        clientKey
    )

    /**
     * Constructor that copies an existing configuration.
     *
     * @param configuration A configuration to initialize the builder.
     */
    constructor(configuration: ConfigurationT) : this(
        configuration.shopperLocale,
        configuration.environment,
        configuration.clientKey
    )

    /**
     * Add configuration for 3DS2 action.
     */
    final override fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): BuilderT {
        genericActionConfigurationBuilder.add3ds2ActionConfiguration(configuration)
        return this as BuilderT
    }

    /**
     * Add configuration for Await action.
     */
    final override fun addAwaitActionConfiguration(configuration: AwaitConfiguration): BuilderT {
        genericActionConfigurationBuilder.addAwaitActionConfiguration(configuration)
        return this as BuilderT
    }

    /**
     * Add configuration for QR code action.
     */
    final override fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): BuilderT {
        genericActionConfigurationBuilder.addQRCodeActionConfiguration(configuration)
        return this as BuilderT
    }

    /**
     * Add configuration for Redirect action.
     */
    final override fun addRedirectActionConfiguration(configuration: RedirectConfiguration): BuilderT {
        genericActionConfigurationBuilder.addRedirectActionConfiguration(configuration)
        return this as BuilderT
    }

    /**
     * Add configuration for Voucher action.
     */
    final override fun addVoucherActionConfiguration(configuration: VoucherConfiguration): BuilderT {
        genericActionConfigurationBuilder.addVoucherActionConfiguration(configuration)
        return this as BuilderT
    }

    /**
     * Add configuration for WeChat Pay action.
     */
    final override fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): BuilderT {
        genericActionConfigurationBuilder.addWeChatPayActionConfiguration(configuration)
        return this as BuilderT
    }
}
