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
 * @param builderShopperLocale The Locale of the shopper.
 * @param builderEnvironment   The [Environment] to be used for network calls to Adyen.
 * @param builderClientKey     Your Client Key used for network calls from the SDK to Adyen.
 */
abstract class BaseConfigurationBuilder<ConfigurationT : Configuration>(
    var builderShopperLocale: Locale,
    var builderEnvironment: Environment,
    var builderClientKey: String
) {

    init {
        if (!ValidationUtils.isClientKeyValid(builderClientKey)) {
            throw CheckoutException("Client key is not valid.")
        }
    }

    /**
     * Constructor that provides default values.
     *
     * @param context A Context
     * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
     */
    constructor(context: Context, clientKey: String) : this(LocaleUtil.getLocale(context), Environment.TEST, clientKey)

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
     * @param builderShopperLocale the [Locale] used for translations.
     * @return The builder instance to chain calls.
     */
    open fun setShopperLocale(builderShopperLocale: Locale): BaseConfigurationBuilder<ConfigurationT> {
        this.builderShopperLocale = builderShopperLocale
        return this
    }

    /**
     * @param builderEnvironment The [Environment] used for network calls.
     * @return The builder instance to chain calls.
     */
    open fun setEnvironment(builderEnvironment: Environment): BaseConfigurationBuilder<ConfigurationT> {
        this.builderEnvironment = builderEnvironment
        return this
    }

    protected abstract fun buildInternal(): ConfigurationT

    fun build(): ConfigurationT {
        if (!ValidationUtils.doesClientKeyMatchEnvironment(builderClientKey, builderEnvironment)) {
            throw CheckoutException("Client key does not match the environment.")
        }

        if (!LocaleUtil.isValidLocale(builderShopperLocale)) {
            throw CheckoutException("Invalid shopper locale: $builderShopperLocale.")
        }

        return buildInternal()
    }
}
