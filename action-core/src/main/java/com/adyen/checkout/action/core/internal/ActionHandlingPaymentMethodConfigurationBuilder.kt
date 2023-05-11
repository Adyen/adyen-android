/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/5/2023.
 */

package com.adyen.checkout.action.core.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.components.core.internal.BaseConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.LocaleUtil
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
    >
/**
 * Initialize a configuration builder with the required fields.
 *
 * @param shopperLocale The [Locale] of the shopper.
 * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
 * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
 */
constructor(
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
     * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
     *
     * @param context A Context
     * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
     * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
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
