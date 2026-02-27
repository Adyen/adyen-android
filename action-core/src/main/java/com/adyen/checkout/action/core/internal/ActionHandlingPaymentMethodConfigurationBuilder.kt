/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/5/2023.
 */

package com.adyen.checkout.action.core.internal

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.await.old.AwaitConfiguration
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.old.RedirectConfiguration
import com.adyen.checkout.threeds2.old.Adyen3DS2Configuration
import com.adyen.checkout.twint.action.TwintActionConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import java.util.Locale

@Suppress(
    "UNCHECKED_CAST",
    "ktlint:standard:discouraged-comment-location",
    "ktlint:standard:type-parameter-list-spacing",
    "ktlint:standard:kdoc",
)
abstract class ActionHandlingPaymentMethodConfigurationBuilder<
    ConfigurationT : Configuration,
    BuilderT : BaseConfigurationBuilder<ConfigurationT, BuilderT>
    >
/**
 * Initialize a configuration builder with the required fields and a shopper locale.
 *
 * @param shopperLocale The [Locale] of the shopper.
 * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
 * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
 */
constructor(
    shopperLocale: Locale?,
    environment: Environment,
    clientKey: String
) : BaseConfigurationBuilder<ConfigurationT, BuilderT>(shopperLocale, environment, clientKey),
    ActionHandlingConfigurationBuilder<BuilderT> {

    protected val genericActionConfigurationBuilder = GenericActionConfiguration.Builder(
        environment = environment,
        clientKey = clientKey,
    ).apply {
        shopperLocale?.let {
            setShopperLocale(it)
        }
    }

    /**
     * Initialize a configuration builder with the required fields.
     *
     * The shopper locale will match the value passed to the API with the sessions flow, or the primary user locale
     * on the device otherwise. Check out the
     * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
     * this value.
     *
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     */
    constructor(
        environment: Environment,
        clientKey: String
    ) : this(
        shopperLocale = null,
        environment = environment,
        clientKey = clientKey,
    )

    /**
     * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
     *
     * @param context A Context
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
     */
    @Deprecated("You can omit the context parameter")
    constructor(
        @Suppress("unused")
        context: Context,
        environment: Environment,
        clientKey: String
    ) : this(
        null,
        environment,
        clientKey,
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
     * Add configuration for Twint action.
     */
    final override fun addTwintActionConfiguration(configuration: TwintActionConfiguration): BuilderT {
        genericActionConfigurationBuilder.addTwintActionConfiguration(configuration)
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
