package com.adyen.checkout.components.base

import android.content.Context
import com.adyen.checkout.components.util.ValidationUtils
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.util.LocaleUtil
import java.util.Locale

/**
 * Base constructor with the required fields.
 *
 * @param shopperLocale The Locale of the shopper.
 * @param environment   The [Environment] to be used for network calls to Adyen.
 * @param clientKey     Your Client Key used for network calls from the SDK to Adyen.
 */
abstract class BaseConfigurationBuilder<
    ConfigurationT : Configuration,
    BuilderT : BaseConfigurationBuilder<ConfigurationT, BuilderT>
    >(
    protected var shopperLocale: Locale,
    protected var environment: Environment,
    protected var clientKey: String
) {

    init {
        if (!ValidationUtils.isClientKeyValid(clientKey)) {
            throw CheckoutException("Client key is not valid.")
        }
    }

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

    protected abstract fun buildInternal(): ConfigurationT

    fun build(): ConfigurationT {
        if (!ValidationUtils.doesClientKeyMatchEnvironment(clientKey, environment)) {
            throw CheckoutException("Client key does not match the environment.")
        }

        if (!LocaleUtil.isValidLocale(shopperLocale)) {
            throw CheckoutException("Invalid shopper locale: $shopperLocale.")
        }

        return buildInternal()
    }
}
