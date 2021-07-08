package com.adyen.checkout.components.base

import android.content.Context
import com.adyen.checkout.components.util.ValidationUtils.isClientKeyValid
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.util.LocaleUtil
import java.util.*

/**
 * Base constructor with the required fields.
 *
 * @param shopperLocale The Locale of the shopper.
 * @param environment   The {@link Environment} to be used for network calls to Adyen.
 * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
 */
abstract class BaseConfigurationBuilder<ConfigurationT : Configuration>(
    var builderShopperLocale: Locale,
    var builderEnvironment: Environment,
    var builderClientKey: String
) {

    /**
     * Constructor that provides default values.
     *
     * @param context A Context
     * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
     */
    constructor(context: Context, clientKey: String) : this(LocaleUtil.getLocale(context), Environment.TEST, clientKey)

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
        if (!isClientKeyValid(builderClientKey, builderEnvironment)) {
            throw CheckoutException("Client key is not valid.")
        }
        return buildInternal()
    }
}
